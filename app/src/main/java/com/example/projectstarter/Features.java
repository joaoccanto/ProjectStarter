package com.example.projectstarter;

public class Features {
    public static double minimum(double data[]) {
        if(data == null || data.length == 0) return 0.0;
        double MIN = data[0];
        for(int i = 1; i < data.length; i++){
            MIN = (data[i] < MIN && data[i] != 0) ? data[i] : MIN;
        }
        return MIN;
    }

    public static double maximum(double data[]) {
        if(data == null || data.length == 0) return 0.0;
        double MAX = data[0];
        for(int i = 1; i < data.length; i++){
            MAX = data[i] > MAX ? data[i] : MAX;
        }
        return MAX;
    }

    public static double mean(double data[]) {
        if(data == null || data.length == 0) return 0.0;
        double SUM = 0;
        for(int i = 1; i < data.length; i++){
            SUM = SUM + data[i];
        }
        return SUM/data.length;
    }

    public static double variance(double data[]) {
        if(data == null || data.length == 0) return 0.0;
        double mean = mean(data);
        double temp = 0.0;

        for(int i = 1; i < data.length; i++){
            temp += (data[i] - mean) * (data[i] - mean);
        }
        return temp/(data.length - 1);
    }

    public static double stdDev(double data[]) {
        if(data == null || data.length == 0) return 0.0;
        double variance = variance(data);
        return Math.sqrt(variance);
    }

    public static double energy(double[] data){
        if(data == null || data.length == 0) return 0.0;
        double sum = 0;
        for(int i=0;i<data.length;i++){
            sum+= Math.pow(data[i],2);
        }
        return sum/data.length;
    }

    public static double zeroCrossingRate(double data[]){
        int length = data.length;
        double num = 0;
        for (int i = 0; i < length - 1; i++)
        {
            if (data[i] * data[i + 1]< 0){
                num++;
            }
        }
        return num / length;
    }

    public static double magnitude(double x, double y, double z){
        return Math.sqrt((x * x) + (y * y) + (z*z));
    }
}
