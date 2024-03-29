package app.algoritmos.util;

import java.util.Comparator;

import app.modelo.Proceso;

public class OrdenarPorTArribo implements Comparator<Proceso>{

	@Override
	public int compare(Proceso p1, Proceso p2) {
		if (p1.getTArribo() == p2.getTArribo())
			return 0;
		else if (p1.getTArribo() > p2.getTArribo())
			return 1;
		else
			return -1;
	}

}
