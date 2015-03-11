/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sloca.json;

import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sloca.model.User;
import sloca.model.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import sloca.model.SharedSecretManager;

/**
 *
 * @author g3t2
 */
/**
 *
 * to process JSON authenticate
 */
public class authenticate extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods, and process authenticating
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
        String username = request.getParameter("username");
        String email = username + "@%";
        String password = request.getParameter("password");

        //creates a new gson object
        //by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject output = new JsonObject();
        User user = UserDAO.retrieveUser(email);

        if (user != null && user.getPassword().equals(password)) {
            //create user token
            String token = JWTUtility.sign(SharedSecretManager.getSharedSecretKeyUser(), "user");
            output.addProperty("status", "success");
            output.addProperty("token", token);
        } else if ("admin".equals(username) && "adminG3T2Number1".equals(password)) {
            //create admin token, only admin token can be used to retrieve data
            String token = JWTUtility.sign(SharedSecretManager.getSharedSecretKeyAdmin(), "admin");
            output.addProperty("status", "success");
            output.addProperty("token", token);

        } else {
            //display error when username and password do not match
            output.addProperty("status", "error");
            JsonArray arr = new JsonArray();
            arr.add(new JsonPrimitive("invalid username/password"));

            output.add("messages", arr);
        }

        out.println(gson.toJson(output));
        out.close();

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
