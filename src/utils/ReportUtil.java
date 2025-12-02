/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import models.contratos;
import models.servicio;
import java.util.ArrayList;
/**
 *
 * @author Raul
 */
public class ReportUtil {
    // Método estático para generar el JSON de contratos que se usará como dato del bloque
    public static String generateJsonForBlock(ArrayList<contratos> listaContratos) {
        StringBuilder reporte = new StringBuilder();
        reporte.append("{\n");
        reporte.append("  \"contratos\": [\n");
        
        for (int i = 0; i < listaContratos.size(); i++) {
            contratos c = listaContratos.get(i);
            reporte.append("    {\n");
            reporte.append("      \"idContrato\": \"").append(c.getIdContrato()).append("\",\n");
            reporte.append("      \"cliente\": \"").append(c.getParteA()).append("\",\n");
            reporte.append("      \"proveedor\": \"").append(c.getParteB()).append("\",\n");
            reporte.append("      \"valorTotal\": ").append(c.getValorTotal()).append(",\n");
            reporte.append("      \"servicios\": [\n");
            
            for (int j = 0; j < c.getListaServicios().size(); j++) {
                servicio s = c.getListaServicios().get(j);
                reporte.append("        {\n");
                reporte.append("          \"idServicio\": \"").append(s.getIdServicio()).append("\",\n");
                reporte.append("          \"descripcion\": \"").append(s.getDescripcion()).append("\",\n");
                reporte.append("          \"estado\": \"").append(s.getEstado()).append("\",\n");
                reporte.append("          \"monto\": ").append(s.getMontoServicio()).append("\n");
                reporte.append("        }");
                if (j < c.getListaServicios().size() - 1) reporte.append(",");
                reporte.append("\n");
            }
            reporte.append("      ]\n");
            reporte.append("    }");
            if (i < listaContratos.size() - 1) reporte.append(",");
            reporte.append("\n");
        }
        reporte.append("  ]\n");
        reporte.append("}\n");
        
        return reporte.toString();
    }
    
    public static String generateHtmlReport(ArrayList<contratos> listaContratos) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html lang=\"es\">\n");
        // ... (El resto del HEAD y los estilos, se mueve directamente desde tu código) ...
        htmlContent.append("<head>\n");
        htmlContent.append("    <meta charset=\"UTF-8\">\n");
        htmlContent.append("    <title>Reporte de Contratos</title>\n");
        htmlContent.append("    <style>\n");
        htmlContent.append("        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; color: #333; }\n");
        htmlContent.append("        .container { max-width: 900px; margin: auto; padding: 20px; background-color: #fff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        htmlContent.append("        h1 { text-align: center; color: #4CAF50; }\n");
        htmlContent.append("        .contrato { border: 1px solid #ddd; padding: 15px; margin-bottom: 20px; border-radius: 5px; background-color: #fafafa; }\n");
        htmlContent.append("        .contrato h2 { color: #2C3E50; margin-top: 0; }\n");
        htmlContent.append("        .servicio { border-left: 3px solid #7f8c8d; padding-left: 10px; margin: 10px 0; }\n");
        htmlContent.append("        .servicio h3 { color: #34495e; }\n");
        htmlContent.append("    </style>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");
        htmlContent.append("    <div class=\"container\">\n");
        htmlContent.append("        <h1>Reporte de Contratos y Servicios</h1>\n");

        for (contratos c : listaContratos) {
            htmlContent.append("        <div class=\"contrato\">\n");
            htmlContent.append("            <h2>Contrato: ").append(c.getIdContrato()).append("</h2>\n");
            htmlContent.append("            <p><strong>Cliente:</strong> ").append(c.getParteA()).append("</p>\n");
            htmlContent.append("            <p><strong>Proveedor:</strong> ").append(c.getParteB()).append("</p>\n");
            htmlContent.append("            <p><strong>Valor Total:</strong> $").append(String.format("%.2f", c.getValorTotal())).append("</p>\n");

            if (!c.getListaServicios().isEmpty()) {
                htmlContent.append("            <h3>Servicios:</h3>\n");
                for (servicio s : c.getListaServicios()) {
                    htmlContent.append("            <div class=\"servicio\">\n");
                    htmlContent.append("                <h4>Servicio: ").append(s.getIdServicio()).append("</h4>\n");
                    htmlContent.append("                <p><strong>Descripción:</strong> ").append(s.getDescripcion()).append("</p>\n");
                    htmlContent.append("                <p><strong>Estado:</strong> ").append(s.getEstado()).append("</p>\n");
                    htmlContent.append("                <p><strong>Monto:</strong> $").append(String.format("%.2f", s.getMontoServicio())).append("</p>\n");
                    htmlContent.append("            </div>\n");
                }
            }
            htmlContent.append("        </div>\n");
        }

        htmlContent.append("    </div>\n");
        htmlContent.append("</body>\n");
        htmlContent.append("</html>\n");
        
        return htmlContent.toString();
    }
    
}
