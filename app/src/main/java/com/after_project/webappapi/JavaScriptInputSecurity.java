package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
public class JavaScriptInputSecurity {
    private Boolean prohibitIpv6 = false;
    private String[] allowedDomains = null;
    private ArrayList ignoreJavascriptStrings = new ArrayList(){{
        add(".");
        add("console.log");
        add("fn.requesturl");//no case Sensitive is needed.
    }};
    JavaScriptInputSecurity(String[] allowedDomains){
        this.allowedDomains = allowedDomains;
    }
    void addIgnoreJavascriptString(String string){
        if(!ignoreJavascriptStrings.contains(string)) {
            ignoreJavascriptStrings.add(string);
        }
    }
    Boolean containsSquareBracketsIpv6InJavaScript(String js){
        String[] squareBracketsArray = js.split("\\[");
        int squareBracketsCount = 0;
        for (String squareBracketsItem: squareBracketsArray) {
            int close_bracket_pos =  squareBracketsItem.indexOf("]");
            if(close_bracket_pos > -1){
                String squareBracketData =  squareBracketsItem.substring(0,close_bracket_pos);
                if(!isArray(squareBracketData)){
                    if(isIpv6(squareBracketData)){
                        return true;
                    }
                }
            }
            squareBracketsCount++;
        }
        return false;
    }
    private Boolean isIpv6(String s){
        String[] arr = s.split("(,{0})+");
        if(arr.length >=2 && s.indexOf("{") == -1){
            return true;
        }
        return false;
    }
    private Boolean isArray(String s){
        try {
            new JSONArray("[" + s + "]");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
    Boolean isAllowedDomainsInJavaScriptString(String js){
        Boolean Continue = true;
        ArrayList javascripStringDomains = findDomains(js.toLowerCase());
        ArrayList filteredDomainList = filterDomainsList(javascripStringDomains,ignoreJavascriptStrings);
        if(allowedDomains!=null)  {
            int foundStringsCount = 0;
            for (Object javascriptDomain: filteredDomainList) {
                for (String serverDomain : allowedDomains) {
                    if (javascriptDomain.equals(serverDomain)) {
                        foundStringsCount++;
                    } else {
                    }
                }
            }
            if(foundStringsCount == filteredDomainList.size()){
            } else {
                Continue = false;
            }
        }
        return Continue;
    }
    private ArrayList filterDomainsList(ArrayList strings, ArrayList ignoredStrings){
        ArrayList list = new ArrayList();
        for(Object string: strings){
            Boolean ignore = false;
            for(Object ignoreString : ignoredStrings){
                if(string.equals(ignoreString)){
                    ignore = true;
                    break;
                }
            }
            if (!ignore){
                list.add(string);
            }
        }
        return list;
    }
    private String getDomain(String host){
        String[] ar = host.split("\\.");
        if(ar.length ==2){
            return host;
        }
        return ar[ar.length-2]+"."+ar[ar.length-1];
    }
    private ArrayList findDomains(String js){
        String string = new String(js);
        ArrayList stringList = new ArrayList();
        while(string.indexOf(".") > -1) {
            String cpleft = "";
            String cprigth = "";
            {
                int dot_pos = string.indexOf(".");
                String domainCharacters = "abcdefghijklmnopqrstuvwxyz0123456789.-";
                if((dot_pos+1) < string.length()) {
                    if (domainCharacters.contains(String.valueOf(string.charAt(dot_pos + 1)))) {
                        for (int i = dot_pos + 1; i < string.length(); i++) {
                            char c = string.charAt(i);
                            if (!domainCharacters.contains(String.valueOf(c))) {
                                break;
                            }
                            cprigth += c;
                        }
                    } else {
                    }
                }
                if(dot_pos-1>-1){
                    if( domainCharacters.contains(String.valueOf(string.charAt(dot_pos-1))) ) {
                        for (int i = dot_pos - 1; i >= 0; i--) {
                            char c = string.charAt(i);
                            if (!domainCharacters.contains(String.valueOf(c))) {
                                break;
                            }
                            cpleft = c + cpleft;
                        }
                    }else{
                        cprigth = "";
                    }
                }
                String cp = cpleft+"."+cprigth;
                stringList.add(cp);
            }
            String sleft = string.substring(0,string.indexOf(".")-cpleft.length());
            String sright= string.substring(string.indexOf(".")+cprigth.length()+1,string.length());
            string = sleft+sright;
        }
        return stringList;
    }
    protected void prohibitIpv6(Boolean prohibitIpv6) {
        this.prohibitIpv6 = prohibitIpv6;
    }
}