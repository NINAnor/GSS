/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.gss.server.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hortonmachine.dbs.log.Logger;

import com.hydrologis.gss.server.GssWorkspace;
import com.hydrologis.kukuratus.libs.servlets.Status;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;

@WebServlet(urlPatterns = "/datadownload")
public class DataDownloadServlet extends HttpServlet {
    private static final String TAG = DataDownloadServlet.class.getSimpleName();

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 10);

        Logger logDb = KukuratusWorkspace.getInstance().getLogDb();
        String ipAddress = "unknown";
        String deviceId = "unknown";
        try {
            if ((deviceId = ServletUtils.canProceed(request, response)) == null) {
                return;
            }

            String fileName = request.getParameter("name");
            if (fileName == null) {
                // send list
                String mapsListJson = GssWorkspace.INSTANCE.getMapsListJson();
                response.setHeader("Content-Type", "application/json");
                ServletOutputStream outputStream = response.getOutputStream();
                PrintStream bou = new PrintStream(outputStream);
                bou.println(mapsListJson);
                bou.close();
                outputStream.flush();
            } else {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", "attachment; filename=" + fileName);

                Optional<File> mapFileOpt = GssWorkspace.INSTANCE.getMapFile(fileName);
                if (mapFileOpt.isPresent()) {
                    File file = mapFileOpt.get();
                    try (InputStream i = new FileInputStream(file); OutputStream o = response.getOutputStream()) {
                        byte[] buffer = new byte[8192];
                        int size = i.read(buffer);
                        while(size > -1) {
                            o.write(buffer, 0, size);
                            size = i.read(buffer);
                        }
                    }
                }

            }

        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Data download connection from '" + deviceId + "' at ip:" + ipAddress + " errored with:\n",
                        ex);
                /*
                 * if there are problems, return some information.
                 */
                String msg = "An error occurred while downloading data from the server.";
                Status errStatus = new Status(Status.CODE_500_INTERNAL_SERVER_ERROR, msg, ex);
                errStatus.sendTo(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
