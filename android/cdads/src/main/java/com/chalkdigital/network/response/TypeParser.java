package com.chalkdigital.network.response;

public class TypeParser {
    public static Double parseDouble(Object o, Double defaultValue){
            if (o!=null) {
                if (o instanceof Double)
                    return (Double)o;
                else if (o instanceof Float)
                    return Double.parseDouble(new Float((float)o).toString());
                else if (o instanceof Integer)
                    return (Double) o;
                else if (o instanceof String)
                    return Double.parseDouble((String) o);
            }
        return defaultValue;
    }

    public static Boolean parseBoolean(Object o, Boolean defaultValue){
        if (o!=null) {
                if (o instanceof Boolean)
                    return (Boolean) o;
                else if(o instanceof Integer) {
                    if ((Integer) o == 1)
                        return true;
                }else if(o instanceof String)
                    return Boolean.parseBoolean((String) o);
        }
        return defaultValue;
    }

    public static Long parseLong(Object o, Long defaultValue){
        if (o!=null) {
            if (o instanceof Long)
                return (long) o;
            else if (o instanceof Integer)
                return ((Integer) o).longValue();
            else if (o instanceof Float)
                return ((Float) o).longValue();
            else if (o instanceof Double)
                return ((Double) o).longValue();
            else if (o instanceof String)
                return Long.parseLong((String) o);
            else if (o instanceof Boolean)
                return ((Boolean)o)?1L:0L;
        }
        return defaultValue;
    }

    public static Integer parseInteger(Object o, Integer defaultValue){
            if (o!=null) {
                if (o instanceof Integer)
                    return (int) o;
                else if (o instanceof Float)
                    return ((Float) o).intValue();
                else if (o instanceof Double)
                    return ((Double) o).intValue();
                else if (o instanceof String)
                    return Integer.parseInt((String) o);
                else if (o instanceof Boolean)
                    return ((Boolean)o)?1:0;
            }
        return defaultValue;
    }
    public static String parseString(Object o, String defaultValue){
        if (o!=null && o instanceof String){
            return  (String) o;
        }
        if (String.valueOf(o).equals("null"))
            return defaultValue;
        return String.valueOf(o);
    }
    public static Float parseFloat(Object o, Float defaultValue){
            if (o!=null) {
                if (o instanceof Float)
                    return  (Float) o;
                else if (o instanceof Integer)
                    return new Float((int)o);
                else if (o instanceof String)
                    return Float.parseFloat((String) o);
                else if (o instanceof Double)
                    return ((Double) o).floatValue();
            }
        return defaultValue;
    }
}
