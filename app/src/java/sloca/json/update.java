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
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import sloca.model.BootStrapManager;
import sloca.model.BootstrapError;
import sloca.model.Location;
import sloca.model.SharedSecretManager;

/**
 *
 * @author g3t2
 */
/**
 *
 * to process the update of data
 */
public class update extends HttpServlet {

    final static int BUFFER = 2048;
    File repository = null;

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
        //creates a new gson object
        //by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //creats a new json object for printing the desired json output
        ArrayList<String> errList = new ArrayList<String>();
        JsonArray errMsg = new JsonArray();

        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Set factory constraints
        // factory.setSizeThreshold(yourMaxMemorySize);
        // factory.setRepository(yourTempDirectory);
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // upload.setSizeMax(yourMaxRequestSize);
        ServletContext servletContext
                = this.getServletConfig().getServletContext();
        File repository = (File) servletContext.getAttribute(
                "javax.servlet.context.tempdir");
        // Parse the request
        List<FileItem> uploadItems = null;
        try {
            uploadItems = upload.parseRequest(request);
        } catch (FileUploadException ex) {
            errList.add("invalid file");
        }
        String value = "";
        for (FileItem uploadItem : uploadItems) {
            if (uploadItem.isFormField()) {
                String fieldName = uploadItem.getFieldName();
                value = uploadItem.getString();
            }
        }
        JsonObject jsonOutput = new JsonObject();
        //validate token
        if (value == null) {

            errList.add("missing token");
        } else if (value.isEmpty()) {

            errList.add("blank token");
        } else {
            try {
                JWTUtility.verify(value, SharedSecretManager.getSharedSecretKeyAdmin());
            } catch (JWTException e) {
                errList.add("invalid token");
            }
        }
        Iterator<FileItem> iter = uploadItems.iterator();
        while (iter.hasNext()) {
            FileItem item = iter.next();

            if (!item.isFormField()) {
                InputStream is = item.getInputStream();

                ZipInputStream zis
                        = new ZipInputStream(
                                new BufferedInputStream(is));
                ZipEntry ze;

                while ((ze = zis.getNextEntry()) != null) {
                    String filePath
                            = repository + File.separator + ze.getName();
                    if (ze.getName().equals("demographics.csv")
                            | ze.getName().equals("location.csv")) {
                        int count;
                        byte data[] = new byte[BUFFER];
                        FileOutputStream fos
                                = new FileOutputStream(filePath);
                        BufferedOutputStream bos
                                = new BufferedOutputStream(fos, BUFFER);
                        while ((count = zis.read(data, 0, BUFFER))
                                != -1) {
                            bos.write(data, 0, count);
                        }
                        bos.flush();
                        bos.close();
                    }
                }
                zis.close();
            }
        }

        File[] files = new File(repository.toString()).listFiles();

        boolean isLocLookUpInserted = false;
        boolean isDemoInserted = false;
        boolean isLocInserted = false;

        boolean isValidFile = false;
        //check if zip files contain the any of the 3 data files
        for (File file : files) {

            String fileName = file.getName();
            if (fileName.contains("demographics.csv")) {
                isValidFile = true;
            } else if (fileName.contains("location.csv")) {
                isValidFile = true;
            }
        }
        //if uploaded folder does not contain the files, it will go back to bootstrap.jsp
        if (!isValidFile) {
            errList.add("invlaid file");
        }

        if (errList.isEmpty()) {

            BootStrapManager bm = new BootStrapManager();
            for (File file : files) {
                String fileName = file.getName();
                String filePath = repository + File.separator + fileName;
                //out.println("</br>" + filePath);
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                    br.readLine();
                    String line = null;
                    if (!isDemoInserted && fileName.contains("demographics.csv")) {
                        bm.updateDemo(filePath);
                        isDemoInserted = true;

                    }

                    if (!isLocInserted && fileName.contains("location.csv")) {
                        bm.updateLoc(filePath);
                        isLocInserted = true;
                    }

                    br.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        br.close();
                    }
                }

                file.delete();
            }
            //error List
            ArrayList<BootstrapError> locErrorList = BootStrapManager.locErrorList;
            //ArrayList<BootstrapError> locLookUpErrorList = BootStrapManager.locLookUpErrorList;
            ArrayList<BootstrapError> demoErrorList = BootStrapManager.demoErrorList;
            Collections.sort(locErrorList);
            //Collections.sort(locLookUpErrorList);
            Collections.sort(demoErrorList);

            if (locErrorList.isEmpty() && demoErrorList.isEmpty()) {
                jsonOutput.addProperty("status", "success");

                //add the array to output
                jsonOutput.add("num-record-loaded", getNumLoaded());

            } else {

                jsonOutput.addProperty("status", "error");

                jsonOutput.add("num-record-loaded", getNumLoaded());

                //create a json array of error
                JsonArray error = new JsonArray();
                //check demogrpahics file
                if (!demoErrorList.isEmpty()) {
                    for (BootstrapError bse : demoErrorList) {
                        JsonObject demoError = new JsonObject();
                        demoError.addProperty("file", "demographics.csv");
                        demoError.addProperty("line", bse.getLineNum());
                        //get Errors
                        ArrayList<String> bseErr = bse.getErrMsg();
                        JsonArray errMsg2 = new JsonArray();
                        for (String e : bseErr) {
                            errMsg2.add(new JsonPrimitive(e));
                        }
                        demoError.add("message", errMsg2);
                        error.add(demoError);
                    }
                }

                //check location file
                if (!locErrorList.isEmpty()) {
                    for (BootstrapError bse : locErrorList) {
                        JsonObject demoError = new JsonObject();
                        demoError.addProperty("file", "location.csv");
                        demoError.addProperty("line", bse.getLineNum());
                        //get Errors
                        ArrayList<String> bseErr = bse.getErrMsg();
                        JsonArray errMsg2 = new JsonArray();
                        for (String e : bseErr) {
                            errMsg2.add(new JsonPrimitive(e));
                        }
                        demoError.add("message", errMsg2);
                        error.add(demoError);
                    }

                }
                //check location lookup file
//                if (!locLookUpErrorList.isEmpty()) {
//                    for (BootstrapError bse : locLookUpErrorList) {
//                        JsonObject demoError = new JsonObject();
//                        demoError.addProperty("file", "location-lookup.csv");
//                        demoError.addProperty("line", bse.getLineNum());
//                        //get Errors
//                        ArrayList<String> bseErr = bse.getErrMsg();
//                        JsonArray errMsg2 = new JsonArray();
//                        for (String e : bseErr) {
//                            errMsg2.add(new JsonPrimitive(e));
//                        }
//                        demoError.add("message", errMsg2);
//                        error.add(demoError);
//                    }
//                }
                jsonOutput.add("error", error);

            }
            //display output
            out.println(gson.toJson(jsonOutput));

        } else {
            jsonOutput.addProperty("status", "error");
            //sorting the errlist into alphabetical order
            HashSet hs = new HashSet();
            hs.addAll(errList);
            errList.clear();
            errList.addAll(hs);

            Collections.sort(errList);
            //loop through the errors to add into the json error array
            for (String err : errList) {
                errMsg.add(new JsonPrimitive(err));
            }

            jsonOutput.add("messages", errMsg);

            out.println(gson.toJson(jsonOutput));

        }
        out.close();
    }

    /**
     *
     * to get th number of data loaded
     *
     * @return a JsonArray to show the number of data loaded in each files
     */
    private JsonArray getNumLoaded() {
        //success list/map
        HashMap<String, Location> locSuccess = BootStrapManager.locSucUpdate;
        //HashMap<String, String> loclookupSuc = BootStrapManager.loclookupSuc;
        ArrayList<String> demoUpdateSuc = BootStrapManager.demoUpdateSuc;
        JsonArray toDisplay = new JsonArray();
        //create jsonobject to add into json array for demo
        JsonObject toAddDemo = new JsonObject();
        toAddDemo.addProperty("demographics", demoUpdateSuc.size());
        toDisplay.add(toAddDemo);
        //create jsonobject to add into json array for location
        JsonObject toAddLoc = new JsonObject();
        toAddLoc.addProperty("location.csv", locSuccess.size());
        toDisplay.add(toAddLoc);
        //create a jsonobject to add into json array for loclookuo
//        JsonObject toAddLocLookUp = new JsonObject();
//        toAddLocLookUp.addProperty("location_lookup.csv", loclookupSuc.size());
//        toDisplay.add(toAddLocLookUp);

        return toDisplay;
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
