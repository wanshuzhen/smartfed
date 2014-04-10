/*
Copyright 2014 ISTI-CNR
 
This file is part of SmartFed.

SmartFed is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
 
SmartFed is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with SmartFed. If not, see <http://www.gnu.org/licenses/>.

*/

package it.cnr.isti.smartfed.metascheduler.constraints;

import it.cnr.isti.smartfed.metascheduler.Constant;
import it.cnr.isti.smartfed.metascheduler.MSPolicy;
import it.cnr.isti.smartfed.metascheduler.resources.MSApplicationNode;
import it.cnr.isti.smartfed.metascheduler.resources.iface.IMSApplication;
import it.cnr.isti.smartfed.metascheduler.resources.iface.IMSProvider;

public class CostPerVmConstraint extends MSPolicy {

	private double highestVmCost;
	
	public CostPerVmConstraint(double weight, double highestValue) {
		super(weight, MSPolicy.DESCENDENT_TYPE, MSPolicy.LOCAL_CONSTRAINT);
		highestVmCost = highestValue;
	}

	@Override
	public double evaluateGlobalPolicy(IMSApplication app, IMSProvider prov) {
		return 0;
	}
	
	public static double vmCost(MSApplicationNode node, IMSProvider prov){
		double cost;
		double[] costPerVm = (double[]) prov.getCharacteristic().get(Constant.COST_VM);
		char vmtype = (char) node.getComputing().getCharacteristic().get(Constant.VM_TYPE);
		switch (vmtype){
		case 's':
			cost = costPerVm[0];
			break;
		case 'm':
			cost = costPerVm[1];
			break;
		case 'l':
			cost = costPerVm[2];
			break;
		case 'x':
			cost = costPerVm[3];
			break;
		default:
			cost = BudgetConstraint.calculateCost(node, prov);
		}
		
		if (Double.isNaN(cost)) {
			if (node.getDesiredCharacteristic() == null)
				cost = BudgetConstraint.calculateCost(node, prov);
			else
				cost = BudgetConstraint.calculateCostDesired(node, prov);
		}
		
		if (Double.isNaN(cost)){
			cost = BudgetConstraint.calculateCost(node, prov);
		}
		return cost;
	}
	
	@Override
	public double evaluateLocalPolicy(MSApplicationNode node, IMSProvider prov) {
		Double budget = (Double) node.getCharacteristic().get(Constant.BUDGET);
		Double r_cost = vmCost(node, prov);
		Double r_maxCost = (highestVmCost); // * RamConstraint.getHighRamValue());
		
		Double cost = r_cost;
		Double maxCost = r_maxCost;
		double distance;
		try {
			maxCost = (budget > maxCost) ? budget : maxCost; // the max value could be the budget
			distance = evaluateDistance(cost, budget, maxCost);
		} catch (Exception e) {
			distance = RUNTIME_ERROR; // a positive value in order to not consider this constraint
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		if (DEBUG)
			System.out.println("\tEval on cost_per_vm " + cost + "-" + budget + "/" + maxCost + "=" + distance);
		return distance * getWeight();
	}

}
