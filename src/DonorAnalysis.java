import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DonorAnalysis {
    public static void main(String[] args) {

        // initialize two ArrayList<Record> objects
        HashMap<String, HashMap<String, ArrayList<Double>>> zipRecords = new HashMap<>();
        TreeMap<String, TreeMap<String, ArrayList<Double>>> dateRecords = new TreeMap<>();
        // record>

        // read file
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8"));
                BufferedWriter bw1 = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(new File(args[1])), "UTF-8"));
                BufferedWriter bw2 = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(new File(args[2])), "UTF-8"));) {
            String temp;
            while ((temp = br.readLine()) != null) {
                String[] tokens = temp.split("\\|");
                // if the token doesn't have enough token numbers, or there are
                // other ids, or id and amount are not valid, skip

                if (tokens.length != 21 || hasOtherId(tokens) || !validateIDAndAmont(tokens)) {
                    continue;
                }

                String id = tokens[0];
                String zip_code = validateZipCode(tokens);
                String transaction_dt = tokens[13];
                double transaction_amt = Double.parseDouble(tokens[14]);

                if (isDateValid(tokens)) {
                    // if date is valid
                    TreeMap<String, ArrayList<Double>> dateMap = dateRecords.get(id);
                    if (dateMap == null) {
                        dateMap = new TreeMap<String, ArrayList<Double>>();
                        ArrayList<Double> amounts = new ArrayList<>();
                        amounts.add(transaction_amt);
                        dateMap.put(transaction_dt, amounts);
                        dateRecords.put(id, dateMap);
                    } else {

                        ArrayList<Double> amounts = dateMap.get(transaction_dt);
                        if (amounts == null) {
                            amounts = new ArrayList<Double>();
                        }
                        amounts.add(transaction_amt);
                        dateMap.put(transaction_dt, amounts);
                        dateRecords.put(id, dateMap);
                    }
                }

                String zipcode = validateZipCode(tokens);
                if (zipcode != null) {
                    HashMap<String, ArrayList<Double>> zipMap = zipRecords.get(id);

                    if (zipMap == null) {
                        zipMap = new HashMap<String, ArrayList<Double>>();
                        ArrayList<Double> amounts = new ArrayList<>();
                        amounts.add(transaction_amt);
                        zipMap.put(zip_code, amounts);
                        zipRecords.put(id, zipMap);
                    } else {

                        ArrayList<Double> amounts = zipMap.get(zip_code);
                        if (amounts == null) {
                            amounts = new ArrayList<Double>();
                        }
                        amounts.add(transaction_amt);
                        zipMap.put(zip_code, amounts);
                        zipRecords.put(id, zipMap);
                    }

                    ArrayList<Double> list = zipMap.get(zip_code);
                    Collections.sort(list);
                    double total = 0.0;
                    for (Double d : list) {
                        total += d;
                    }
                    double median = list.get(list.size() / 2);
                    // print zip map

                    bw1.write(id + "|" + zip_code + "|" + Math.round(median) + "|" + list.size() + "|"
                            + Math.round(total) + "\n");
                }

            }
            // print the dt
            for (Map.Entry<String, TreeMap<String, ArrayList<Double>>> entry : dateRecords.entrySet()) {
                TreeMap<String, ArrayList<Double>> innerMap = entry.getValue();
                for (Map.Entry<String, ArrayList<Double>> e : innerMap.entrySet()) {
                    ArrayList<Double> list = e.getValue();
                    Collections.sort(list);
                    double total = 0.0;
                    for (Double d : list) {
                        total += d;
                    }
                    double median = list.get(list.size() / 2);
                    bw2.write(entry.getKey() + "|" + e.getKey() + "|" + Math.round(median) + "|" + list.size() + "|"
                            + Math.round(total) + "\n");
                }

            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

    }

    public static boolean hasOtherId(String[] tokens) {

        return !tokens[15].trim().equals("");
    }

    public static boolean isDateValid(String[] tokens) {
        // if it is empty, return false
        String date = tokens[13].trim();
        if (date.equals("")) {
            return false;
        }
        // if it is not eight numbers, return false
        if (!date.matches("^\\d{8}$")) {
            return false;
        }
        int month = Integer.parseInt(date.substring(0, 2));
        int day = Integer.parseInt(date.substring(2, 4));
        int year = Integer.parseInt(date.substring(4));
        if (month > 12 || day > 31) {
            return false;
        }
        // judging day value according to month value
        switch (month) {
        case 2:
            if (year % 4 == 0) {
                return day <= 28;
            }
            return day <= 29;
        case 4:
        case 6:
        case 9:
        case 11:
            return day <= 30;
        default:
            return day <= 31;
        }
    }

    public static String validateZipCode(String[] token) {
        String zipcode = token[10].trim();
        if (!zipcode.matches("^\\d{5}.*")) {
            return null;
        }
        return zipcode.substring(0, 5);
    }

    public static boolean validateIDAndAmont(String[] token) {

        String id = token[0].trim();
        String amt = token[14].trim();

        return !id.equals("") && isNumeric(amt);
    }

    /**
     * Helper function
     */
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
