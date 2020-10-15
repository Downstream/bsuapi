package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.dbal.Cypher;
import bsuapi.settings.OpenPipeSetting;
import bsuapi.settings.OpenPipeSettings;
import bsuapi.settings.SettingGroup;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path( "/settings" )
public class SettingsResource extends BaseResource
{
    @Path( "/{groupName: [a-z]*}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response settingGroup(
        @PathParam("groupName") String groupName,
        @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        JSONObject data = new JSONObject();
        data.put("title","Neo4j JSON API Settings for Group: "+ groupName);

        SettingGroup group;

        try {
            group = SettingGroup.match(groupName);
        } catch (Exception e) {
            return response.badRequest("Invalid Setting Group: "+groupName);
        }

        Cypher c = new Cypher(db);
        OpenPipeSetting current = new OpenPipeSetting(group);

        try {
            data.put(group.key(), current.getData(c));
        } catch (Throwable e) {
            data.put(group.key() +"-exception", JsonResponse.exceptionDetailed(e));
        }

        return response.plain(data);
    }

    @Path( "/raw/{groupName: [a-z]*}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response rawGroup(
            @PathParam("groupName") String groupName,
            @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        JSONObject data = new JSONObject();
        data.put("title","Neo4j JSON API Settings for Group: "+ groupName);

        SettingGroup group;

        try {
            group = SettingGroup.match(groupName);
        } catch (Exception e) {
            return response.badRequest("Invalid Setting Group: "+groupName);
        }

        try (Cypher c = new Cypher(db)) {
            data.put("data", group.query().exec(c));
        } catch (Throwable e) {
            data.put("exception", JsonResponse.exceptionDetailed(e));
        }

        return response.plain(data);
    }

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response settings(
        @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        JSONObject data = new JSONObject();
        data.put("title","Neo4j JSON API Settings");
        data.put("summary","Mode Settings from OpenPipeline, and config of this API.");
        this.attachPackageDetails(data);

        try (Cypher c = new Cypher(db)) {
            data.put("settings", OpenPipeSettings.listAll(c));
        } catch (Throwable e) {
            data.put("settings", new JSONObject());
            data.put("settings-exception", JsonResponse.exceptionDetailed(e));
        }

        return response.plain(data);
    }

    private void attachPackageDetails(JSONObject data)
    {
        data.put("version", Config.getDefault("version", "0.1"));
        data.put("package", Config.getDefault("package", "bsuapi"));
        data.put("canonical", Config.buildUri("/settings"));
    }

    public static BehaviorDescribe describeList()
    {
        return BehaviorDescribe.resource("/settings",
        "Mode Settings from OpenPipeline, and config of this API."
        );
    }

    public static BehaviorDescribe describeSingleGroup()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/settings/{GROUP}",
        "Mode Settings from OpenPipeline for a specific mode."
        );

        desc.arg("GROUP", "name of script to run.");
        desc.put("args", SettingGroup.describeAll());

        return desc;
    }
}
