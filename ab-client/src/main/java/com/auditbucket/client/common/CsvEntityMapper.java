package com.auditbucket.client.common;

import com.auditbucket.client.Importer;
import com.auditbucket.client.csv.CsvColumnDefinition;
import com.auditbucket.client.csv.CsvColumnHelper;
import com.auditbucket.client.rest.AbRestClient;
import com.auditbucket.helper.FlockException;
import com.auditbucket.registration.bean.TagInputBean;
import com.auditbucket.track.bean.EntityInputBean;
import com.auditbucket.track.model.EntityKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mike
 * Date: 27/04/14
 * Time: 4:34 PM
 */
public class CsvEntityMapper extends EntityInputBean implements DelimitedMappable {
    //private static org.slf4j.Logger logger = LoggerFactory.getLogger(CsvTrackMapper.class);

    public CsvEntityMapper(ImportParams importParams) {
        setDocumentType(importParams.getDocumentType());
        setFortress(importParams.getFortress());
        setFortressUser(importParams.getFortressUser());
    }

    @Override
    public Importer.importer getImporter() {
        return Importer.importer.CSV;
    }

    @Override
    public AbRestClient.type getABType() {
        return AbRestClient.type.TRACK;
    }

    private Map<String, Object> toMap(String[] headerRow, String[] line, ImportParams importParams) {
        int col = 0;

        Map<String, Object> row = new HashMap<>();
        for (String column : headerRow) {
            CsvColumnDefinition colDef = importParams.getColumnDef(column);

            //if (colDef.getDateFormat)
            if (NumberUtils.isNumber(line[col])) {
                row.put(column, NumberUtils.createNumber(line[col]));
            } else {
                Date date = null;
//                try {
//                    if ( colDef!=null && colDef.getDateFormat()!=null ) {
//                        date = DateUtils.parseDate(line[col], colDef.getDateFormat());
//                        row.put(column, date.getTime());
//                    }
//                } catch (ParseException e) {
//                    //
//                }
                //if ( date == null ) // Stash it as a string
                row.put(column, line[col]);
            }

            col++;
        }
        return row;
    }

    @Override
    public Map<String, Object> setData(final String[] headerRow, final String[] line, ImportParams importParams) throws JsonProcessingException, FlockException {
        int col = 0;
        Map<String, Object> row = toMap(headerRow, line, importParams);

        for (String column : headerRow) {
            CsvColumnHelper columnHelper = new CsvColumnHelper(column, line[col], importParams.getColumnDef(headerRow[col]));
            if (!columnHelper.ignoreMe()) {
                if (columnHelper.isDescription()) {
                    setDescription(row.get(column).toString());
                }
                if (columnHelper.isCallerRef()) {
                    String callerRef = getCallerRef();
                    if (callerRef == null)
                        callerRef = columnHelper.getValue();
                    else
                        callerRef = callerRef + "." + columnHelper.getValue();

                    setCallerRef(callerRef);
                }
                if (columnHelper.isTag()) {
                    String thisColumn = columnHelper.getKey();

                    String val = columnHelper.getValue();
                    if (val != null && !val.equals("")) {
                        TagInputBean tag;
                        val = columnHelper.getValue();
                        if (columnHelper.isCountry()) {
                            val = importParams.getStaticDataResolver().resolveCountryISOFromName(val);
                        }
                        Map<String, Object> properties = new HashMap<>();
                        if (columnHelper.isValueAsProperty()) {
                            tag = new TagInputBean(thisColumn).setMustExist(columnHelper.isMustExist()).setLabel(thisColumn);

                            if (Integer.decode(columnHelper.getValue()) != 0) {
                                properties.put("value", Integer.decode(columnHelper.getValue()));
                                if (columnHelper.getNameColumn() != null) {
                                    tag.addEntityLink(row.get(columnHelper.getNameColumn()).toString(), properties);
                                } else if (columnHelper.getRelationshipName() != null) {
                                    tag.addEntityLink(columnHelper.getRelationshipName(), properties);
                                } else
                                    tag.addEntityLink("undefined", properties);
                            } else {
                                break; // Don't set a 0 value tag
                            }
                        } else {
                            String index = columnHelper.getKey();

                            tag = new TagInputBean(val).setMustExist(columnHelper.isMustExist()).setLabel(columnHelper.isCountry() ? "Country" : index);
                            tag.addEntityLink(columnHelper.getRelationshipName());
                        }
                        CsvHelper.setNestedTags(tag, columnHelper.getColumnDefinition().getTargets(), row);
                        addTag(tag);
                    }

                }
                if (columnHelper.isTitle()) {
                    setName(line[col]);
                }
            } // ignoreMe
            col++;
        }
        Collection<String> strategyCols = importParams.getStrategyCols();
        for (String strategyCol : strategyCols) {
            CsvColumnDefinition colDef = importParams.getColumnDef(strategyCol);
            String tag = importParams.getStaticDataResolver().resolve(strategyCol, getColumnValues(colDef, row));

            if (tag != null) {
                addCrossReference(colDef.getStrategy(), new EntityKey(colDef.getFortress(), colDef.getDocumentType(), tag));
            }
        }

        if (importParams.getEntityKey() != null){
            CsvColumnDefinition columnDefinition = importParams.getColumnDef(importParams.getEntityKey());
            if (columnDefinition != null) {
                String[] metaCols = columnDefinition.getRefColumns();
                String callerRef = "";
                for (String metaCol : metaCols) {
                    callerRef = callerRef + (!callerRef.equals("") ? "." : "") + row.get(metaCol);
                }
                setCallerRef(callerRef);
            }

        }

        return row;
    }

    private Map<String, Object> getColumnValues(CsvColumnDefinition colDef, Map<String, Object> row) {
        Map<String, Object> results = new HashMap<>();
        String[] columns = colDef.getColumns();

        int i = 0;
        int max = columns.length;
        while (i < max) {
            results.put(columns[i], row.get(columns[i]));
            i++;
        }
        return results;
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    public static DelimitedMappable newInstance(ImportParams importParams) {
        return new CsvEntityMapper(importParams);
    }

    @Override
    public char getDelimiter() {
        return ',';
    }
}
