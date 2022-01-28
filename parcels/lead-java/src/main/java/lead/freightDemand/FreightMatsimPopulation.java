package lead.freightDemand;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Iterator;
import java.util.List;

public class FreightMatsimPopulation {

    static void generateMATSimFreightPopulation(List<Trips> allWeekDayTrips) {

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);
        PopulationFactory pf = scenario.getPopulation().getFactory();
        Population population = scenario.getPopulation();

        int personId = 0;
        int missedPlans = 0;
        for (Trips trip : allWeekDayTrips) {
            Person person = pf.createPerson(Id.createPersonId("truck_" + personId));
            PopulationUtils.putSubpopulation(person, "freight");
            Plan plan = pf.createPlan();
            if (trip instanceof DirectTour) {
                DirectTour directTrip = (DirectTour) trip;
                Activity activity = pf.createActivityFromCoord("truck_operation", directTrip.startPoint.coord);
                plan.addActivity(activity);
                Activity activity2 = pf.createActivityFromCoord("truck_operation", directTrip.entPoint.coord);
                activity2.setStartTime(directTrip.timeSlot);
                plan.addActivity(activity2);
                Activity activity3 = pf.createActivityFromCoord("truck_operation", directTrip.startPoint.coord);
                plan.addActivity(activity3);
            } else if (trip instanceof RoundTour) {
                RoundTour roundTrip = (RoundTour) trip;
                Iterator<Integer> iterator = roundTrip.timeSlots.iterator();
                boolean first = true;
                if (roundTrip.tourPoints.size() != roundTrip.timeSlots.size() +1 ) {

                }
                for (Movement move : roundTrip.tourPoints) {
                    if (first) {
                        first = false;
                        Activity activity = pf.createActivityFromCoord("truck_operation", move.coord);
                        activity.setEndTime(0);
                        plan.addActivity(activity);
                        Leg leg = pf.createLeg("freight");
                        plan.addLeg(leg);
                    } else {
                        Activity activity2 = pf.createActivityFromCoord("truck_operation", move.coord);
                        activity2.setEndTime(iterator.next());
                        plan.addActivity(activity2);
                        Leg leg = pf.createLeg("freight");
                        plan.addLeg(leg);
                    }
                }
                Activity activity = pf.createActivityFromCoord("truck_operation", roundTrip.tourPoints.get(0).coord);
                activity.setEndTime(24*3600);
                plan.addActivity(activity);
            }
            personId++;
            person.addPlan(plan);
            population.addPerson(person);
        }
        System.out.println("Missed plans " + missedPlans);
        new PopulationWriter(population).write(FreightDemand.outputLocation + "_plans.xml");
    }


}