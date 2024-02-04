package com.driver.controllers;


import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import io.swagger.models.auth.In;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AirportController {
    HashMap<String,Airport> airRepo=new HashMap<>();
    HashMap<Integer,Flight> fliRepo=new HashMap<>();
    HashMap<Integer,Passenger> pRepo= new HashMap<>();
    HashMap<Integer,HashSet<Integer>> tRepo= new HashMap<>();
    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport){
        String n= airport.getAirportName();
        airRepo.put(n,airport);

        //Simply add airport details to your database
        //Return a String message "SUCCESS"

        return "SUCCESS";
    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){
        int n=-99999;
        ArrayList<String> arr= new ArrayList<>();
        for(String k: airRepo.keySet())
        {
            if(n<airRepo.get(k).getNoOfTerminals())
            {
                n=airRepo.get(k).getNoOfTerminals();
            }
        }
        for(String k: airRepo.keySet())
        {
            if(n==airRepo.get(k).getNoOfTerminals())
            {
                arr.add(airRepo.get(k).getAirportName());
            }
        }
        Collections.sort(arr);
        if(arr.size()>0)
        return arr.get(0);

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName

       return null;
    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity, @RequestParam("toCity")City toCity){
       double n =9999999999.0000;
       int c=-1;
        for(int k: fliRepo.keySet())
        {
            if(fliRepo.get(k).getToCity().compareTo(toCity)==0 && fliRepo.get(k).getFromCity().compareTo(fromCity)==0)
            {
                if(n>fliRepo.get(k).getDuration())
                {
                    n=fliRepo.get(k).getDuration();
                }
                c++;
            }

        }

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        if(c==-1)
           return -1;
        return n;
    }

    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date,@RequestParam("airportName")String airportName){
        int c=0;
        for(Integer k: tRepo.keySet())
        {
            if(date.compareTo(fliRepo.get(k).getFlightDate() )==0 && (fliRepo.get(k).getFromCity().compareTo(airRepo.get(airportName).getCity())==0 || fliRepo.get(k).getToCity().compareTo(airRepo.get(airportName).getCity())==0))
            {
                c += tRepo.get(k).size();
            }
        }
        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight

        return c;
    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId")Integer flightId){
        int i = tRepo.get(flightId).size() * 50;
        int c=3000+ i;

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price

       return c;

    }


    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){
        if (fliRepo.get(flightId).getMaxCapacity()<=0)
            return "FAILURE";
        if(tRepo.containsKey(flightId))
        {
            HashSet<Integer> hs= tRepo.get(flightId);
            if(hs.size()>=fliRepo.get(flightId).getMaxCapacity())
                return "FAILURE";
            else if (hs.contains(passengerId))
                return "FAILURE";
            else {
                hs.add(passengerId);
                tRepo.put(flightId,hs);
                return "SUCCESS";
            }
        }

            HashSet<Integer> hs=new HashSet<>();
            hs.add(passengerId);
            tRepo.put(flightId,hs);
            return "SUCCESS";

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"


    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){
        if(!fliRepo.containsKey(flightId) ||  !pRepo.containsKey(passengerId) || !tRepo.get(flightId).contains(passengerId))
            return "FAILURE";
        tRepo.get(flightId).remove(passengerId);
        return "SUCCESS";
        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId


    }


    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId")Integer passengerId){
        int c=0;
        for(Integer k: tRepo.keySet())
        {
            if(tRepo.get(k).contains(passengerId))
                c++;
        }
        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
       return c;
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody @NotNull Flight flight){
        int n= flight.getFlightId();
        fliRepo.put(n,flight);
        //Return a "SUCCESS" message string after adding a flight.
       return "SUCCESS";
    }


    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId")Integer flightId){
         if(!fliRepo.containsKey(flightId))
             return  null;
        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName

        return fliRepo.get(flightId).getFromCity().toString();
    }


    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId")Integer flightId){
int sum=0;
int n= tRepo.get(flightId).size();
        for(int i=0;i<n;i++)
        {
            sum += (3000+(i*50));
        }
//Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight


        return sum;
    }


    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger){
        int n= passenger.getPassengerId();
        pRepo.put(n,passenger);
        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully.

       return "SUCCESS";
    }


}
