package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import static com.after_project.webappapi.MessengerConnection.ConnectionStatus.ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND;
import static com.after_project.webappapi.MessengerConnection.ConnectionStatus.ERROR_CONNECTION_TAG_NOT_REGISTERED;
import static com.after_project.webappapi.MessengerConnectionManager.ConnectionType.CONNECTION_MULTCLIENT;
import static com.after_project.webappapi.MessengerConnectionManager.ConnectionType.CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_IF_MATCHS;
import static com.after_project.webappapi.MessengerConnectionManager.ConnectionType.CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES;
import static com.after_project.webappapi.MessengerConnectionManager.ConnectionType.CONNECTION_NORMAL;
import android.os.Messenger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class MessengerConnectionManager {
    private class ConnectionRulesMatch extends RulesMatch {
        ConnectionType connectionType = null;
        ConnectionRulesMatch(String...stringsToMatch){
            super(stringsToMatch);
        }
    }
    private class ConnectionMulticlientMatchByTags extends ConnectionRulesMatch{
        ConnectionMulticlientMatchByTags(String...matchs){
            super(matchs);
            this.rulesMatchType = RulesMatchType.MATCH_TYPE_TAG;
            this.connectionType = CONNECTION_MULTCLIENT;
        }
    }
    private class ConnectionNormalMatchByTags extends ConnectionRulesMatch{
        ConnectionNormalMatchByTags(String...matchs){
            super(matchs);
            this.rulesMatchType = RulesMatchType.MATCH_TYPE_TAG;
            this.connectionType = CONNECTION_NORMAL;
        }
    }
    private class ConnectionNormalMatchByNames extends ConnectionRulesMatch{
        ConnectionNormalMatchByNames(String...matchs){
            super(matchs);
            this.rulesMatchType = RulesMatchType.MATCH_TYPE_NAME;
            this.connectionType = CONNECTION_NORMAL;
        }
    }
    private class ConnectionMulticlientMatchByNames extends ConnectionRulesMatch {
        ConnectionMulticlientMatchByNames(String...matchs){
            super(matchs);
            this.rulesMatchType = RulesMatchType.MATCH_TYPE_NAME;
            this.connectionType = CONNECTION_MULTCLIENT;
        }
    }
    private class ConnectionRules{
        private Boolean parallel = null;
        private Integer parallelLimit = null; 
        private ConnectionRulesMatch[] connectionRulesMatchs = null;
        ConnectionRules(boolean parallel, int parallelLimit){
            this.parallel = parallel;
            this.parallelLimit = parallelLimit;
        }
        ConnectionRules(boolean parallel, int parallelLimit, ConnectionRulesMatch... connectionRulesMatchs ){
            this(parallel,parallelLimit);
            this.connectionRulesMatchs = connectionRulesMatchs;
        }
        Boolean isParallel() {
            return parallel;
        }
        Integer getParallelLimit() {
            return parallelLimit;
        }
    }
    enum ConnectionType {
        CONNECTION_NORMAL, 
        CONNECTION_MULTCLIENT, 
        CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES, //for tests only not use in production
        CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_IF_MATCHS  // for tests only not use in production
    }
    private class ConnectionPolicy{
        private transient Class<?> clazz; 
        private ConnectionType connectionType = null; 
        private Integer maxConnections = null; 
        private ConnectionRules connectionRules = null;
        ConnectionPolicy(Class<?> clazz, ConnectionType connectionType, int maxConnections,  ConnectionRules connectionRules){
            this.clazz = clazz;
            this.connectionType = connectionType;
            this.maxConnections = maxConnections;
            this.connectionRules = connectionRules;
            checkForConnectionPoliceDuplicates(this);
        }
        ConnectionType getConnectionType() {
            return connectionType;
        }
        ConnectionRules getConnectionRules() {
            return connectionRules;
        }
        String getTag() {
            return clazz.getSimpleName();
        }
    }
    private ArrayList<ConnectionPolicy> connectionPolicies = new ArrayList<>();
    private List<AbstractMap.SimpleEntry< Messenger,MessengerConnection>> connections = new ArrayList<>();
    private void checkForConnectionPoliceDuplicates(ConnectionPolicy _connectionPolicy){
        int count = 0;
        for(ConnectionPolicy connectionPolicy: connectionPolicies){
            final String json = new Gson().toJson(connectionPolicy,ConnectionPolicy.class);
            if(json.equals(new Gson().toJson(_connectionPolicy,ConnectionPolicy.class))){
                throw new Error("Connection Police Duplicate found connectionPolicies.index(" + count + ")");
            }else  if(connectionPolicy.clazz.getSimpleName().equals(_connectionPolicy.clazz.getSimpleName())){
                throw new Error("Connection Police for "+_connectionPolicy.clazz.getSimpleName()+" already exists");
            }
            count++;
        }
    }
    MessengerConnectionManager(){
        /*
        CONNECTION_MULTCLIENT is a connection that are able to send response back for any active connection in your application.
        CONNECTION_NORMAL is a connection only send e get back response only for its self , ex : if you add ConnectionPolicy for UltimateRealAppMainActivity with  CONNECTION_NORMAL it will only response back for  UltimateRealAppMainActivity, otherwise using   CONNECTION_MULTCLIENT will response back for all or any others active connections
        HELP USAGE:
        maxConnections are the first limit connection priority
        connectionRules(optional, use null) it is the rule of the connectionPolice. if set parallel TRUE then the limit connections is the priority, use ConnectionRulesMatch for matches with any name or tag you need. if is set to null not is the most secure usage, becase will go accept any connection by the argument of the clazz . use 0 for unlimited connections until reaches the maxConnections
and maxconnections 0 for cancel as unavailable for any connection
use parallel = false for a unique exclusive connection
attention if you CONNECTION_NORMAL then use ConnectionNormalMatchByNames
and if you CONNECTION_MULTCLIENT then use  ConnectionMulticlientMatchByNames
        */
        connectionPolicies.add(new ConnectionPolicy(UltimateRealAppMainActivity.class, CONNECTION_MULTCLIENT,
                3,
                new ConnectionRules(false,1, new ConnectionMulticlientMatchByNames("ultmain") )
        ) );
        connectionPolicies.add(new ConnectionPolicy(UltimateRealAppMyPurchasesActivity.class, ConnectionType.CONNECTION_NORMAL,
                1, new ConnectionRules(false,0, new ConnectionNormalMatchByNames("ultpurchases")))  );
        connectionPolicies.add(new ConnectionPolicy(UltimateRealAppShopActivity.class, ConnectionType.CONNECTION_NORMAL,
                1, new ConnectionRules(false,0, new ConnectionNormalMatchByNames("ultshop")))  );
        connectionPolicies.add(new ConnectionPolicy(UltimateRealAppCustomerProfileActivity.class, CONNECTION_MULTCLIENT,
                1, new ConnectionRules(false,0, new ConnectionMulticlientMatchByNames("ultprofile")))  );
    }
    protected static MessengerConnection.ConnectionStatus ConnectionStatusValueOf(String value) {
        MessengerConnection.ConnectionStatus[] valueEnums = MessengerConnection.ConnectionStatus.values();
        MessengerConnection.ConnectionStatus result = null;
        for (MessengerConnection.ConnectionStatus valueEnum : valueEnums) {
            if(valueEnum.name().equals(value)) {
                result = valueEnum;
                break;
            }
        }
        return result;
    }
    protected static MessengerConnection.ConnectionState ConnectionStateValueOf(String value) {
        MessengerConnection.ConnectionState[] valueEnums = MessengerConnection.ConnectionState.values();
        MessengerConnection.ConnectionState result = null;
        for (MessengerConnection.ConnectionState valueEnum : valueEnums) {
            if(valueEnum.name().equals(value)) {
                result = valueEnum;
                break;
            }
        }
        return result;
    }
    private static ConnectionType ConnectiontypeValueOf(String value) {
        ConnectionType[] valueEnums = ConnectionType.values();
        ConnectionType result = null;
        for (ConnectionType valueEnum : valueEnums) {
            if(valueEnum.name().equals(value)) {
                result = valueEnum;
                break;
            }
        }
        return result;
    }
    private int getActiveConnectionsCount(String tag){
        int tagcount = 0;
        for (int i=0; i<connections.size(); i++){
            if(connections.get(i).getValue().getTag().equals(tag)){
                if(connections.get(i).getValue().getConnectionState().equals(MessengerConnection.ConnectionState.CONNECTION_STATE_OK)) {
                    tagcount++;
                }
            }
        }
        return tagcount;
    }
    private int getActiveParallelConnectionsCount(String tag){
        int tagcount = 0;
        for (int i=0; i<connections.size(); i++){
            if(connections.get(i).getValue().getTag().equals(tag)){
                if(connections.get(i).getValue().getConnectionState().equals(MessengerConnection.ConnectionState.CONNECTION_STATE_OK)) {
                    if(connections.get(i).getValue().isParallel()) {
                        tagcount++;
                    }
                }
            }
        }
        return tagcount;
    }
    private class MessengerConnectionAdapter implements JsonSerializer<MessengerConnection> {
        private boolean isParallel = false;
        MessengerConnectionAdapter(){
        }
        MessengerConnection.ConnectionStatus filterConnection(ConnectionPolicy registry, ConnectionRulesMatch connectionRulesMatch, MessengerConnection connection){
            isParallel = false;
            if(registry.maxConnections==0){
                return MessengerConnection.ConnectionStatus.ERROR_CONNECTION_UNAVAILABLE;
            }
            if(getActiveConnectionsCount(connection.getTag()) < registry.maxConnections) {
                {
                    boolean isMultiCLient = false;
                    int matches = 0;
                    if(registry.connectionRules!=null) {
                        if (registry.connectionRules != null && connectionRulesMatch!=null) {
                            {
                                if (connectionRulesMatch.rulesMatchType.equals(RulesMatchType.MATCH_TYPE_NAME) ) {
                                    if (!connectionRulesMatch.matchWith(connectionRulesMatch.toArray(), connection.getName())) {
                                        if ( ! registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES)) {
                                            return connectionRulesMatch.connectionType.equals(CONNECTION_MULTCLIENT)? MessengerConnection.ConnectionStatus.ERROR_CONNECTION_MULTCLIENT_NO_MATCHS_FOUND: MessengerConnection.ConnectionStatus.ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND;
                                        }
                                    } else {
                                        if(connectionRulesMatch.connectionType.equals(registry.connectionType) ) {
                                            matches++;
                                        }else{
                                            if(! registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES)) {
                                                return ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND;
                                            }
                                        }
                                    }
                                }
                                if (connectionRulesMatch.rulesMatchType.equals(RulesMatchType.MATCH_TYPE_TAG) ) {
                                    if (!connectionRulesMatch.matchWith(connectionRulesMatch.toArray(), connection.getTag())) {
                                        if ( ! registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES)) {
                                            return connectionRulesMatch.connectionType.equals(CONNECTION_MULTCLIENT)? MessengerConnection.ConnectionStatus.ERROR_CONNECTION_MULTCLIENT_NO_MATCHS_FOUND: MessengerConnection.ConnectionStatus.ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND;
                                        }
                                    } else {
                                        if(connectionRulesMatch.connectionType.equals(registry.connectionType) ) {
                                            matches++;
                                        }else{
                                            if(! registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES)) {
                                                return ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (registry.getConnectionRules().isParallel()) {
                        } else {
                            if (getActiveParallelConnectionsCount(registry.getTag()) > 0) {
                                return connectionRulesMatch.connectionType.equals(CONNECTION_MULTCLIENT)? MessengerConnection.ConnectionStatus.ERROR_CONNECTION_MULTCLIENT_ALREADY_CONNECTED : MessengerConnection.ConnectionStatus.ERROR_CONNECTION_NORMAL_ALREADY_CONNECTED;
                            }
                        }
                        if (getActiveParallelConnectionsCount(registry.getTag()) < registry.getConnectionRules().getParallelLimit() || registry.getConnectionRules().getParallelLimit() == 0) {
                            if ( ! registry.connectionType.equals(CONNECTION_NORMAL)) {
                                if (registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES))
                                {
                                    if (((registry.connectionRules != null && matches != 0))) {
                                        isMultiCLient = true;
                                    }
                                }else{
                                    isMultiCLient = true;
                                }
                            }
                            if(connectionRulesMatch!=null && connectionRulesMatch.connectionType.equals(CONNECTION_NORMAL)) {
                                isParallel = true;
                            }
                        } else {
                            return connectionRulesMatch.connectionType.equals(CONNECTION_MULTCLIENT)? MessengerConnection.ConnectionStatus.ERROR_CONNECTION_MULTCLIENT_PARALLEL_LIMIT_EXCEEDED : MessengerConnection.ConnectionStatus.ERROR_CONNECTION_NORMAL_PARALLEL_LIMIT_EXCEEDED;
                        }
                    }
                    else{
                        if(registry.connectionType.equals(CONNECTION_MULTCLIENT)){
                            isMultiCLient = true;
                        }else if(registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_IF_MATCHS)){
                            return  MessengerConnection.ConnectionStatus.ERROR_CONNECTION_MULTCLIENT_NO_MATCHS_FOUND;
                        }
                    }
                    if (registry.connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_IF_MATCHS)){
                        isMultiCLient = false;
                        if( matches > 0 ) { 
                            if( connectionRulesMatch.connectionType.equals(CONNECTION_MULTCLIENT)){
                                isMultiCLient = true;
                            }
                        }
                    }
                    if(isMultiCLient){
                        if(registry.connectionRules!=null) { 
                            isParallel = true;
                        }
                        return MessengerConnection.ConnectionStatus.CONNECTION_MULTICLIENT_SUCCESS;
                    }else{
                        return MessengerConnection.ConnectionStatus.CONNECTION_CLIENT_SUCCESS;
                    }
                }
            }else {
                return MessengerConnection.ConnectionStatus.ERROR_MAX_CONNECTION_EXCEEDED;
            }
        }
        @Override
        public JsonElement serialize(MessengerConnection src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            {
                jsonObject.addProperty("clientId", src.getClientId());
                jsonObject.addProperty("tag", src.getTag());
                jsonObject.addProperty("name", src.getName());
            }
            {
                for (ConnectionPolicy registry : connectionPolicies) {
                    if(registry.getTag().equals(src.getTag())) {
                        int connectionRulesMatchCount = 0;
                        ConnectionType connectionType = ConnectiontypeValueOf(new String(String.valueOf(registry.connectionType)));
                        if(registry.getConnectionType().equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_IF_MATCHS)){
                            if(registry.getConnectionRules()==null ||  registry.getConnectionRules().connectionRulesMatchs.length<2) {
                                return null;
                            }
                            registry.connectionType = CONNECTION_MULTCLIENT;
                        } else if(registry.getConnectionType().equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES)){
                            registry.connectionType = CONNECTION_MULTCLIENT;
                        }
                        MessengerConnection.ConnectionStatus status = filterConnection(registry, registry.getConnectionRules()!=null?registry.getConnectionRules().connectionRulesMatchs[0]:null, src);
                        if(connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_IF_MATCHS)){
                            if(!status.equals(MessengerConnection.ConnectionStatus.CONNECTION_MULTICLIENT_SUCCESS)){
                                registry.connectionType = CONNECTION_NORMAL;
                                if(registry.getConnectionRules().connectionRulesMatchs.length==2) {
                                    status = filterConnection(registry, registry.getConnectionRules().connectionRulesMatchs[1], src);
                                }else{
                                    return null;
                                }
                            }
                        } else if(connectionType.equals(CONNECTION_MULTCLIENT_ONLY_IF_MATCHS_ELSE_CONNECTION_NORMAL_NO_RULES)){
                            if(!status.equals(MessengerConnection.ConnectionStatus.CONNECTION_MULTICLIENT_SUCCESS)) {
                                registry.connectionType = CONNECTION_NORMAL;
                                status = filterConnection(registry, null, src);
                            }
                        }
                        jsonObject.addProperty("connectionStatus", String.valueOf(status));
                        jsonObject.addProperty("isParallel", isParallel);
                        return jsonObject;
                    }
                }
            }
            return null;
        }
    }
    protected JsonObject endConnection(final Integer connectionId){
        String json = new Gson().toJson(connections.get(connectionId).getValue(),MessengerConnection.class);
        JsonObject jsonObjectChanges =  new JsonObject();
        jsonObjectChanges.addProperty("connectionState", String.valueOf(MessengerConnection.ConnectionState.CONNECTION_STATE_ENDED));
        jsonObjectChanges.addProperty("connectionStatus", String.valueOf(MessengerConnection.ConnectionStatus.CONNECTION_CLIENT_DISCONNECTED));
        JsonObject jsonObject =  JsonParser.parseString(json).getAsJsonObject();
        Map<String, JsonElement> map = jsonObject.asMap();
        map.putAll(jsonObjectChanges.asMap());
        connections.get(connectionId).setValue(new Gson().fromJson(jsonObject,MessengerConnection.class));
        return jsonObjectChanges;
    }
    protected void addConnection(final MessengerConnection requestConnection, final android.os.Message msg, final MessengerConnection.MessengerConnectionCallback connectionCallback){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(MessengerConnection.class,new MessengerConnectionAdapter())
                .create();
          String json = gson.toJson(requestConnection);
          if(json instanceof String){
          }else{
          }
          JsonObject jsonObject = null;
          if( !json.equals("null") ) {
              jsonObject = JsonParser.parseString(json).getAsJsonObject();
              MessengerConnection.ConnectionStatus status = ConnectionStatusValueOf(jsonObject.get("connectionStatus").getAsString());
              {
                  if((status.equals(MessengerConnection.ConnectionStatus.CONNECTION_CLIENT_SUCCESS) || status.equals(MessengerConnection.ConnectionStatus.CONNECTION_MULTICLIENT_SUCCESS))){
                          AbstractMap.SimpleEntry<android.os.Messenger,MessengerConnection> entry = new AbstractMap.SimpleEntry<>(msg.replyTo, null);
                          connections.add(entry);
                          final int connectionId = new Integer(connections.lastIndexOf(entry));
                          jsonObject.addProperty("connectionState", String.valueOf(MessengerConnection.ConnectionState.CONNECTION_STATE_OK));
                          jsonObject.addProperty("isMultiClient",status.equals(MessengerConnection.ConnectionStatus.CONNECTION_MULTICLIENT_SUCCESS));
                          jsonObject.addProperty("connectionId",connectionId);
                          entry.setValue(new Gson().fromJson(jsonObject,MessengerConnection.class));
                      connectionCallback.onConnectionSuccess(entry.getValue(), status);
                  }else{
                      connectionCallback.onConnectionError(ConnectionStatusValueOf(jsonObject.get("connectionStatus").getAsString()));
                  }
              }
          }else{
              connectionCallback.onConnectionError(ERROR_CONNECTION_TAG_NOT_REGISTERED);
          }
    }
}