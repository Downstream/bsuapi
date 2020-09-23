package bsuapi.space;

import org.neo4j.graphdb.spatial.Coordinate;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.DoubleValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.virtual.MapValue;

import java.util.*;

/*
   Just some simplifications for converting and handling:
   org.neo4j.values.storable.PointValue :: point({x: [float], y: [float], crs: 'wgs-84'})

   currently DOES NOT work with anything other than wgs-84
 */

public class PointConversion
{
    private static final int EARTH_R = 6371; //km

    @UserFunction
    @Description("bsuapi.space.averagePoints([point, ...]) find average geo point from list of points. Returns map for point arg: WITH point(bsuapi.space.averagePoints([])) as avgPoint")
    public Map averagePoints(
        @Name("point") List<PointValue> points
    )
    {
        double[] total = {0,0,0};
        for (PointValue point : points) {
            double[] cart3 = this.pointToCartesian(point);
            total[0] += cart3[0];
            total[1] += cart3[1];
            total[2] += cart3[2];
        }

        double[] avgCart3 = {
            total[0] / points.size(),
            total[1] / points.size(),
            total[2] / points.size(),
        };

        return this.coordsToPointMap(this.cartesianToCoords(avgCart3));
    }

    private double[] coordsToCartesian(double[] coord2)
    {
        double latRad = Math.toRadians(coord2[0]);
        double lonRad = Math.toRadians(coord2[1]);

        return new double[] {
                EARTH_R * Math.cos(latRad) * Math.cos(lonRad),
                EARTH_R * Math.cos(latRad) * Math.sin(lonRad),
                EARTH_R * Math.sin(latRad)
        };
    }

    private double[] pointToCartesian(PointValue point)
    {
        List<Double> coords = point.getCoordinate().getCoordinate();
        double latRad = Math.toRadians(coords.get(0));
        double lonRad = Math.toRadians(coords.get(1));

        return new double[] {
            EARTH_R * Math.cos(latRad) * Math.cos(lonRad),
            EARTH_R * Math.cos(latRad) * Math.sin(lonRad),
            EARTH_R * Math.sin(latRad)
        };
    }

    private double[] cartesianToCoords(double[] cart3)
    {
        double latRad = Math.asin(cart3[2] / EARTH_R);
        double lonRad = Math.atan2(cart3[1], cart3[0]);

        return new double[] {
            Math.toDegrees(latRad),
            Math.toDegrees(lonRad)
        };
    }

    private Map coordsToPointMap(double[] coord2)
    {
        return this.toPointMap(coord2[0], coord2[1]);
    }

    private Map toPointMap(double x, double y)
    {
        //convert to point in Cypher: WITH point(bsuapi.space.averagePoints([point, ...])) as avgPoint
        HashMap<String, Object> result = new HashMap();
        result.put("x", x);
        result.put("y", y);
        result.put("crs", "wgs-84");
        return result;
    }

    private boolean isGoodPoint(Object point)
    {
        return
            point instanceof PointValue  &&
            ((PointValue) point).getCoordinateReferenceSystem().equals(CoordinateReferenceSystem.WGS84)
            ;
    }
}
