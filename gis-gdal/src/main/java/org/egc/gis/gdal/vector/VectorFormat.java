package org.egc.gis.gdal.vector;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.egc.gis.gdal.FormatConversion;
import org.egc.gis.gdal.IOFactory;
import org.egc.gis.gdal.dto.GDALDriversEnum;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;

import java.io.IOException;

/**
 * @author houzhiwei
 * @date 2020/7/9 17:44
 */
@Slf4j
public class VectorFormat implements FormatConversion {
    /**
     * Gemetry to geojson string.
     *
     * @param srcFile the src file
     * @return the geojson string
     */
    public String toGeoJson(String srcFile) {
        DataSource source = IOFactory.createVectorIO().read(srcFile);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.GetLayerCount(); i++) {
            Layer layer = source.GetLayer(i);
            Feature feature = layer.GetNextFeature();
            sb.append(feature.GetGeometryRef().ExportToJson());
        }
        source.delete();
        return sb.toString();
    }

    /**
     * To geojson file.
     *
     * @param srcFile the src file
     * @param dstFile the dst file
     * @return the GeoJSON file
     */
    public void toGeoJsonFile(String srcFile, String dstFile) throws IOException {
        formatConvert(srcFile, dstFile, GDALDriversEnum.GeoJSON);
    }

    /**
     * To kml file.
     *
     * @param srcFile the src file
     * @return the KML file
     */
    public String toKmlFile(String srcFile, String dstFile) throws IOException {
        return formatConvert(srcFile, dstFile, GDALDriversEnum.KML);
    }

    /**
     * To kml string.
     *
     * @param srcFile the src file
     * @return the KML String
     */
    public String toKml(String srcFile) throws IOException {
        DataSource source = IOFactory.createVectorIO().read(srcFile);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.GetLayerCount(); i++) {
            Layer layer = source.GetLayer(i);
            Feature feature = layer.GetNextFeature();
            sb.append(feature.GetGeometryRef().ExportToKML());
        }
        source.delete();
        return sb.toString();
    }

    /**
     * To WKT string.
     *
     * @param srcFile the src file
     * @return the WKT String
     * @throws IOException the io exception
     */
    public String toWkt(String srcFile) throws IOException {
        DataSource source = IOFactory.createVectorIO().read(srcFile);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.GetLayerCount(); i++) {
            Layer layer = source.GetLayer(i);
            Feature feature = layer.GetNextFeature();
            sb.append(feature.GetGeometryRef().ExportToWkt());
        }
        source.delete();
        return sb.toString();
    }

    /**
     * vector format conversion, guess format from dst extention
     *
     * @param srcFile
     * @param dstFile
     * @throws IOException
     */
    @Override
    public String formatConvert(String srcFile, String dstFile) throws IOException {
        String ext = FilenameUtils.getExtension(dstFile);
        log.info("Convert from {} to {}", srcFile, dstFile);
        GDALDriversEnum driversEnum = GDALDriversEnum.lookupByExtension(ext);
        return formatConvert(srcFile, dstFile, driversEnum);
    }

    /**
     * vector format conversion
     *
     * @param srcFile
     * @param format
     * @throws IOException
     */
    @Override
    public String formatConvert(String srcFile, String dstFile, GDALDriversEnum format) throws IOException {
        gdal.AllRegister();
        ogr.RegisterAll();
        DataSource datasource = ogr.Open(srcFile);
        log.info("Convert from {} to {} format", srcFile, format.name());
        Driver driver = ogr.GetDriverByName(format.name());
        if (driver == null) {
            log.error("Output Format {} Not Supported", format.name());
            throw new IOException("Output Format " + format.name() + " Not Supported");
        }
        String ext = format.getExtension();
        String newName = outputName(srcFile, dstFile, ext);
        driver.CopyDataSource(datasource, newName);
        datasource.delete();
        driver.delete();
        return newName;
    }
}
