package org.foodscanner.foodscanner.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.foodscanner.foodscanner.Constants;

import java.util.Locale;

/**
 * Class for managing database operations.
 */
public class DatabaseManager extends SQLiteAssetHelper {
    public static final String TAG = "FoodScannerV2-DB";

    private static final String DATABASE_NAME = "db_aditiv.db";
    private static final int DATABASE_VERSION = 1;

    private static final String ADDITIVE_TABLE = "Aditiv";
    private static final String DANGER_COLUMN = "stopnjaNevarnosti";

    private static final String NAME_TABLE = "NazivAditiva";
    public static final String ID_COLUMN = "_id";
    private static final String LANGUAGE_COLUMN = "jezik";
    public static final String NAME_COLUMN = "naziv";

    private static final String DESCRIPTION_TABLE = "OpisAditiva";
    private static final String DESCRIPTION_COLUMN = "opis";

    private final String USER_LANGUAGE;


    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //TODO: Remove hardcoding
        USER_LANGUAGE = "sl";
    }

    /**
     * Searches the database with the specified name or part of name and returns the result. The
     * result, which includes the id and name columns, also returns any partial matches.
     *
     * @param name additive name or part of name
     * @return cursor containing query result (id and name columns only)
     */
    public Cursor findAdditiveByName(String name) {
        return findAdditive(name, true);
    }

    /**
     * Searches the database with the specified E-number and returns the result. The result includes
     * the id and name table columns.
     *
     * @param eNumber additive E-number
     * @return cursor containing query result (id and name columns only)
     */
    public Cursor findAdditiveByENumber(String eNumber) {
        return findAdditive(eNumber, false);
    }

    /**
     * Queries the database for the additive's danger level and description for a given E-Number.
     * The result of the query is returned in a Cursor.
     *
     * @param eNumber additive E-number
     * @return cursor containing query result (danger level and description columns only)
     */
    public Cursor getExtrasForENumber(String eNumber) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = { DANGER_COLUMN, DESCRIPTION_COLUMN };
        String sqlFrom =
                ADDITIVE_TABLE + " LEFT JOIN " + DESCRIPTION_TABLE + " USING( " + ID_COLUMN + " ) ";
        String sqlWhere = ID_COLUMN + " = ? AND (" + LANGUAGE_COLUMN + " = ? OR " + LANGUAGE_COLUMN + " IS NULL) AND 1 = " + Constants.IS_ADDITIVE_DATABASE_ENABLED;
        String[] sqlWhereArgs = { eNumber, USER_LANGUAGE };

        qb.setTables(sqlFrom);
        Cursor c = qb.query(db, sqlSelect, sqlWhere, sqlWhereArgs, null, null, null);
        c.moveToFirst();

        return c;
    }

    /**
     * Searches the database with the specified name or E-number. The result of the search is then
     * returned and it is composed of the id and name columns. When searching with a name, partial
     * results (matches) are included.
     *
     * @param searchParameter either name or E-number to perform search with
     * @param isByName flag that specifies if the search is performed by name
     * @return cursor containing query result (id and name columns only)
     */
    private Cursor findAdditive(String searchParameter, boolean isByName) {
        SQLiteDatabase db = getReadableDatabase();

        String[] sqlSelect = { ID_COLUMN, NAME_COLUMN };
        String sqlFrom = NAME_TABLE;
        String sqlWhere = isByName? "UPPER(" + NAME_COLUMN + ") LIKE UPPER(?)" : "UPPER(" + ID_COLUMN + ") LIKE UPPER(?)";
        sqlWhere += " AND " + LANGUAGE_COLUMN + " = ? AND 1 = " + Constants.IS_ADDITIVE_DATABASE_ENABLED;
        searchParameter = isByName? "%" + searchParameter + "%" : searchParameter;
        String[] sqlWhereArgs = {searchParameter, USER_LANGUAGE};

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(sqlFrom);

        Cursor c = qb.query(db, sqlSelect, sqlWhere, sqlWhereArgs, null, null, null);
        c.moveToFirst();

        return c;
    }

    public Cursor getAllAdditives() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String tables = String.format("%1$s LEFT JOIN %2$s USING(%3$s) LEFT JOIN %4$s USING(%3$s)", ADDITIVE_TABLE, NAME_TABLE, ID_COLUMN, DESCRIPTION_TABLE);
        Log.d(TAG, "FROM " + tables);
        qb.setTables(tables);
        //TODO: Add name table language check ;)
        String where = String.format(Locale.ENGLISH, "(%1$s = '%2$s' OR %1$s IS NULL) AND 1 = %3$d", DESCRIPTION_TABLE + "." + LANGUAGE_COLUMN, USER_LANGUAGE, Constants.IS_ADDITIVE_DATABASE_ENABLED);
        Log.d(TAG, "WHERE " + where);
        qb.appendWhere(where);
        Cursor c = qb.query(db, null, null, null, null, null, null);
        c.moveToFirst();

        return c;
    }
}
