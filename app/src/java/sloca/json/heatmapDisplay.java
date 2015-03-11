/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sloca.model.User;

/**
 *
 * @author admin
 */
public class heatmapDisplay extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/JSON");
        PrintWriter out = response.getWriter();
        HttpSession sess = request.getSession();
        User u = (User) sess.getAttribute("LoggedInUser");
        HashMap<String, Integer> toDisplay = (HashMap<String, Integer>) sess.getAttribute("heatmap");
        //creates a new gson object
        //by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //creats a new json object for printing the desired json output
        JsonObject jsonOutput = new JsonObject();
        if (u != null && toDisplay != null) {
            //display for user interface heatmap
            jsonOutput.addProperty("status", "success");
            //creates the jsonarray to be printed later
            JsonArray heatMapToPrint = new JsonArray();
            Iterator<String> ite = toDisplay.keySet().iterator();
            //iterate through the hashmap
            while (ite.hasNext()) {
                String semanticplace = ite.next();
                int numPeople = toDisplay.get(semanticplace);
                //store attributes into a jsonobject
                JsonObject tempObject = new JsonObject();
                tempObject.addProperty("semantic-place", semanticplace);
                tempObject.addProperty("num-people", numPeople);
                tempObject.addProperty("crowd-density", getDensity(numPeople));
                heatMapToPrint.add(tempObject);
            }
            //add the json array
            jsonOutput.add("heatmap", heatMapToPrint);
            //print with pretty prints!
            out.println(gson.toJson(jsonOutput));
            //sess.removeAttribute("heatmap");

        }
    }

    /**
     *
     * to get the density number
     *
     * @param num is the number of people
     * @return the density number
     */
    private int getDensity(int num) {
        if (num == 0) {
            return 0;
        } else if (num <= 2) {
            return 1;
        } else if (num <= 5) {
            return 2;
        } else if (num <= 10) {
            return 3;
        } else if (num <= 20) {
            return 4;
        } else if (num <= 30) {
            return 5;
        } else if (num > 30) {
            return 6;
        }
        return 0;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
