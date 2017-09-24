package org.openecomp.sdc.toscaparser.api;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
//Generate excel file, include all validation issues errors in jtosca
//the error java code, the line number and file name for each error.
public class GetValidationIssues {

    public static CSVWriter fileWriter = null;
    public static List<String[]> data = new ArrayList<>();

    public static void main(String[] args) {
    	System.out.println("GetAllValidationIssues - path to project files Directory is " + Arrays.toString(args));
        File jtoscaFiles = new File(args[0]+ "\\jtosca\\src\\main\\java\\org\\openecomp\\sdc\\toscaparser\\api");

        try {
            printFiles(jtoscaFiles);
            fileWriter = new CSVWriter(new FileWriter(args[1]+"\\JToscaValidationIssues_"+System.currentTimeMillis()+".csv"), '\t');
            fileWriter.writeNext(new String[] {"Error Message", "Class Name", "Line No."}, false);
            fileWriter.writeAll(data, false);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    private static void printFiles(File dir) {
        if (dir != null && dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory())
                    printFiles(file);
                else {
                    Scanner scanner = null;
                    try {
                        scanner = new Scanner(file);

                        int lineNum = 0;
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            lineNum++;
                            if (line.startsWith("/*python"))
                                break;

                            if (!line.trim().startsWith("//") && !line.trim().startsWith("#") && line.contains("ThreadLocalsHolder.getCollector().appendValidationIssue")) {
                                String errMsg = line.trim();
                                if (!errMsg.contains(";")) {
                                    String nextLine = null;
                                    while (scanner.hasNextLine() && (nextLine == null || !nextLine.contains(";"))) {
                                        nextLine = scanner.nextLine();
                                        errMsg += nextLine.trim();
                                    }
                                }

                                data.add(new String[]{errMsg, file.getName(), String.valueOf(lineNum)});
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

