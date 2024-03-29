package app.algoritmos.sjf;

import java.util.ArrayList;
import java.util.Collections;

import app.algoritmos.util.OrdenarPorCPU1;
import app.algoritmos.util.OrdenarPorTArribo;
import app.modelo.ElementoTablaParticion;
import app.modelo.ElementoTablaProceso;
import app.modelo.Particion;
import app.modelo.ProcesoSJF;
import javafx.collections.ObservableList;

public class SJFFijasBestFit {

	private static ArrayList<ArrayList<Particion>> mapaMemoria;

	private static ArrayList<Integer> ganttCpu;

	private static int[] salida;
	private static int[] arribo;
	private static int[] irrupcion;

	/*
	 * Devuelve el estado de las aprticiones para armar el mapa de memoria
	 */
	public static ArrayList<ArrayList<Particion>> getMapaMemoria() {
		return mapaMemoria;
	}

	/*
	 * Devuelve los procesos para armar el gantt
	 */
	public static ArrayList<Integer> getGanttCpu() {
		return ganttCpu;
	}

	/*
	 * Devuelve los tiempos de salida, arribo e irrupcion para calcular las
	 * estadisticas.
	 * 
	 * En la posicion 0 de salida guardo el t ocioso.
	 * 
	 */
	public static int[] getSalida() {
		return salida;
	}

	public static int[] getArribo() {
		return arribo;
	}

	public static int[] getIrrupcion() {
		return irrupcion;
	}

	/*
	 * EJECUTAR
	 * 
	 */
	public static void ejecutar(ObservableList<ElementoTablaParticion> tablaParticiones,
			ObservableList<ElementoTablaProceso> tablaProcesos) {

		System.out.println("SJF - Particiones Fijas - BestFit\n");

		ArrayList<Particion> particiones = new ArrayList<Particion>();
		ArrayList<ProcesoSJF> procesos = new ArrayList<ProcesoSJF>();

		ArrayList<ProcesoSJF> nuevos = new ArrayList<ProcesoSJF>();
		ArrayList<ProcesoSJF> listos = new ArrayList<ProcesoSJF>();

		ArrayList<ProcesoSJF> ejecutandoCpu = new ArrayList<ProcesoSJF>();

		// Inicializamos mapaMemoria
		mapaMemoria = new ArrayList<ArrayList<Particion>>();

		// Inicializamos ganttCpu
		ganttCpu = new ArrayList<Integer>();

		/*
		 * Cargamos la lista de Particiones
		 */
		for (ElementoTablaParticion p : tablaParticiones) {
			Particion particion = new Particion(p.getId(), p.getTamanio(), true);
			particiones.add(particion);
		}

		/*
		 * Cargamos la lista de Procesos
		 */
		int tIrrupcion = 0; // Para controlar el bucle principal

		for (ElementoTablaProceso p : tablaProcesos) {
			ProcesoSJF proceso = new ProcesoSJF(p.getId(), p.getTamanio(), p.getTArribo(), p.getCpu1(), p.getEs1(),
					p.getCpu2(), p.getEs2(), p.getCpu3(), p.getPrioridad());
			procesos.add(proceso);
			tIrrupcion += proceso.getCpu1();
		}

		Collections.sort(procesos, new OrdenarPorTArribo());
		int idUltimoProceso = procesos.get(procesos.size() - 1).getId();

		// Inicializamos el arreglo de los t de salida
		salida = new int[(procesos.size() + 1)];

		// Cargamos el arreglo de arribos e irrupcion
		arribo = new int[(procesos.size() + 1)];
		irrupcion = new int[(procesos.size() + 1)];

		for (int i = 0; i < procesos.size(); i++) {
			arribo[procesos.get(i).getId()] = procesos.get(i).getTArribo();
			irrupcion[procesos.get(i).getId()] = procesos.get(i).getCpu1();
		}

		/*
		 * ARRANCA EL ALGORITMO
		 * 
		 */

		int t = 0; // Reloj
		int tOcioso = 0;
		boolean llegoElUltimo = false;

		while (t <= tIrrupcion + tOcioso) {

			/*
			 * ARMO LA COLA DE NUEVOS DEL INSTANTE t
			 * 
			 */
			for (ProcesoSJF p : procesos) {
				if (p.getTArribo() == t) {
					nuevos.add(p);
					if (p.getId() == idUltimoProceso)
						llegoElUltimo = true;
				}
			}

			/*
			 * EJECUTO CPU
			 * 
			 */
			if (!ejecutandoCpu.isEmpty()) {
				ejecutarCpu(particiones, procesos, ejecutandoCpu, tablaProcesos, t);
			}

			/*
			 * TIEMPO OCIOSO
			 * 
			 */
			if (nuevos.isEmpty() && ejecutandoCpu.isEmpty() && !llegoElUltimo) {
				tOcioso++;
			}

			/*
			 * ARMO LA COLA DE LISTOS EN INSTANTE t
			 * 
			 */
			
			for (int i = 0; i < nuevos.size(); i++) {
				ProcesoSJF pNuevo = nuevos.get(i);
				
				int tamanioBestFit = Integer.MAX_VALUE; // Para guardar el tamanio del mejor ajuste
				int posicionBestFit = 0; // Para guardar el ID de la particion con mejor ajuste
				
				// Recorro la lista de particiones para encontrar el mejor ajuste
				for (Particion particion : particiones) {
					if (particion.isLibre() && pNuevo.getTamanio() <= particion.getTamanio() && particion.getTamanio() < tamanioBestFit) {
						tamanioBestFit = particion.getTamanio();
						posicionBestFit = particion.getId();
					}
				}
				
				// Si la encuentro, le asigno el proceso nuevo
				if (posicionBestFit != 0) {
					for (Particion particion: particiones) {
						if (particion.getId() == posicionBestFit) {
							listos.add(pNuevo);
							particion.setProceso(pNuevo.getId());
							particion.setLibre(false);
							nuevos.remove(i);
							i--;// Para evitar ConcurrentModificationException
							break;
						}
					}
				}
				
			}

			/*
			 * AGREGO LOS LISTOS A EJECUCION
			 * 
			 */
			for (int i = 0; i < listos.size(); i++) {
				ProcesoSJF pListo = listos.get(i);
				ejecutandoCpu.add(pListo);
				listos.remove(i);
				i--; // Para evitar ConcurrentModificationException
			}

			/*
			 * TIEMPO OCIOSO EN GANTT
			 * 
			 */
			if (ejecutandoCpu.isEmpty())
				ganttCpu.add(0);

			/*
			 * GUARDO EL ESTADO DE LAS PARTICIONES
			 * 
			 */
			mapaMemoria.add(t, new ArrayList<Particion>());
			for (Particion p : particiones) {
				Particion particion = new Particion(p.getId(), p.getTamanio(), p.getProceso(), p.isLibre());
				mapaMemoria.get(t).add(particion);
			}

			t++;

		} // Fin While

		salida[0] = tOcioso;

	} // Fin SJF

	/*
	 * METODO EJECUTAR CPU
	 * 
	 */
	private static void ejecutarCpu(ArrayList<Particion> particiones, ArrayList<ProcesoSJF> procesos,
			ArrayList<ProcesoSJF> ejecutandoCpu, ObservableList<ElementoTablaProceso> tablaProcesos, int t) {

		/*
		 * Veo si el primero se esta ejcutando, si es asi lo saco momentaneamente y
		 * ordeno el resto de los procesos segun menor tiempo remanente
		 * 
		 * Sino ordeno directamente
		 * 
		 */
		if (ejecutandoCpu.get(0).getEstaEjecutando()) {
			ProcesoSJF temporal = ejecutandoCpu.get(0);
			ejecutandoCpu.remove(0);
			Collections.sort(ejecutandoCpu, new OrdenarPorCPU1());
			ejecutandoCpu.add(0, temporal);
		} else {
			Collections.sort(ejecutandoCpu, new OrdenarPorCPU1());
			ejecutandoCpu.get(0).setEstaEjecutando(true);
		}

		ProcesoSJF procesoActual = ejecutandoCpu.get(0);
		int cpu = procesoActual.getCpu1();
		cpu--;

		// Actualizo Gantt
		ganttCpu.add(procesoActual.getId());

		// Actualizo el valor de CPU1
		procesoActual.setCpu1(cpu);
		ejecutandoCpu.remove(0);
		ejecutandoCpu.add(0, procesoActual);

		if (cpu == 0) {

			// Libero la particion
			for (Particion particion : particiones) {
				if (particion.getProceso() == procesoActual.getId()) {
					particion.setProceso(0);
					particion.setLibre(true);
					break;
				}
			}

			// Guardo el t de salida
			salida[procesoActual.getId()] = t;

			// Luego saco el proceso
			ejecutandoCpu.remove(0);

		}

	}

}
