package com.pholema.persist.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;

import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.log4j.Logger;

import com.pholema.persist.models.LdapUser;
import com.pholema.persist.models.SalesPersonManager;
import com.pholema.tool.utils.dynamic.PropertiesUtils;
import com.pholema.tool.utils.management.Ldap;

public class SalesPersonManagerStore extends CacheStoreAdapter<String, SalesPersonManager> {

    private final static Logger logger = Logger.getLogger(SalesPersonManagerStore.class);

    private static String connect_sqlServer = PropertiesUtils.properties.getProperty("db.jdbc.salesPerson.url");
    //debug
//    private static String dbSchema = "Temptable.dbo.NESO_CRM_Agent_Relationship_temp";
    private static String dbSchema = "EHISSQL.CRM.dbo.NESO_CRM_Agent_Relationship";

    @Override
    public SalesPersonManager load(String key) throws CacheLoaderException {
        SalesPersonManager salesPersonManager = null;

        String script = " SELECT Agent, Manager "
                + " FROM " + dbSchema + " WITH (NOLOCK) "
                + " WHERE Status = 'O' and Agent = ? ";

        try {
            logger.info("Get data start...");
            Connection conn = DBConnection.generateConnection(connect_sqlServer);
            ResultSet res;
            try (PreparedStatement st = conn.prepareStatement(script)) {
                st.setString(1, key);
                res = st.executeQuery();
                while (res.next()) {
                    salesPersonManager = revealSalesPersonManager(res);
                    logger.info("Get data complete-->" + res.getString(1));
                }
            }
            DBConnection.closeResultSet(res);
            DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load: " + key, e);
        }
        return salesPersonManager;
    }

    @Override
    public void delete(Object key) throws CacheWriterException {
        char status = 'V';
        String script = "UPDATE " + dbSchema
                + " set Status=?"
                + " WHERE Agent = ? ";
        try {
            Connection conn = DBConnection.generateConnection(connect_sqlServer);
            try (PreparedStatement st = conn.prepareStatement(script)) {
                st.setObject(1, status, Types.CHAR);
                st.setString(2, (String)key);
                st.executeUpdate();
            }
            DBConnection.closeConnection(conn);
            System.out.println("change status complete");
        } catch (SQLException e) {
            throw new CacheWriterException("Failed to delete: " + key, e);
        }
    }

    @Override
    public void write(Cache.Entry<? extends String, ? extends SalesPersonManager> entry) throws CacheWriterException {
        char status = 'O';
        write(Collections.singletonList(entry.getValue()), status);
    }

    private void write(List<SalesPersonManager> list, char status) throws CacheWriterException {
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String countryCode = "USB";
            int companyCode = 1003;
            Connection conn = DBConnection.generateConnection(connect_sqlServer);
            // Syntax of MERGE statement is database specific and should be adopted for your database.
            // If your database does not support MERGE statement then use sequentially update, insert statements.
            StringBuilder sb = new StringBuilder();
            sb.append("declare @Agent varchar(15) \r\n");
            sb.append("declare @Manager varchar(15) \r\n");
            sb.append("declare @Status char \r\n");
            sb.append("declare @CountryCode varchar(15) \r\n");
            sb.append("declare @CompanyCode int \r\n");
            sb.append("declare @InDate datetime \r\n");
            sb.append("declare @InUser varchar(15) \r\n");
            sb.append("declare @LastEditDate datetime \r\n");
            sb.append("declare @LastEditUser varchar(15) \r\n");
            sb.append("select @Agent=?, @Manager=?, @Status=?, @CountryCode=?, @CompanyCode=?, @InDate=?, @InUser=?, @LastEditDate=?, @LastEditUser=? \r\n");
            sb.append("if Not exists(select top 1 1 from " + dbSchema + " where Agent=@Agent) \r\n");
            sb.append("   begin \r\n");
            sb.append("      insert into " + dbSchema + "(" +
                    //"TransactionNumber," + // _tmp
                    "Agent,Manager,Status,CountryCode,CompanyCode,InDate,InUser,LastEditDate,LastEditUser) " +
                    "values(" +
                    //"(select COUNT(1) from " + dbSchema + ") + 1," + // _tmp
                    "@Agent,@Manager,@Status,@CountryCode,@CompanyCode,@InDate,@InUser,@LastEditDate,@LastEditUser) \r\n");
            sb.append("   end \r\n");
            sb.append("else \r\n");
            sb.append("   begin \r\n");
            sb.append("      update " + dbSchema + " set Manager=@Manager, Status=@Status, LastEditDate=@LastEditDate, LastEditUser=@LastEditUser where Agent=@Agent \r\n");
            sb.append("   end \r\n");

            try (PreparedStatement st = conn.prepareStatement(sb.toString())) {
                for (SalesPersonManager v : list) {
                    st.setString(1, v.getSalesPerson());
                    st.setString(2, v.getManager().equals(v.getSalesPerson()) ? "NULL" : v.getManager());
                    st.setObject(3, status, Types.CHAR);
                    st.setString(4, countryCode);
                    st.setInt(5, companyCode);
                    st.setTimestamp(6, timestamp);
                    st.setString(7, v.getUserLeadingAdding());
                    st.setTimestamp(8, timestamp);
                    st.setString(9, v.getUserLeadingAdding());
                    st.addBatch();
                }
                st.executeBatch();
            }
            DBConnection.closeConnection(conn);
            System.out.println("insert/update finished " + list.size());
        } catch (SQLException e) {
            logger.error("fail " , e);
            throw new CacheWriterException("Failed to write");
        }
    }

    // This method is called whenever "loadCache()" and "localLoadCache()"
    // methods are called on IgniteCache. It is used for bulk-loading the cache.
    // If you don't need to bulk-load the cache, skip this method.
    @Override
    public void loadCache(IgniteBiInClosure<String, SalesPersonManager> clo, Object... args) {
        String script =" SELECT Agent, Manager"
                + " FROM " + dbSchema + " WITH (NOLOCK) "
                + " WHERE Status = 'O'";

        Integer defined_entry_count;
        if (args == null || args.length == 0 || args[0] == null) {
            defined_entry_count = 2000000;
        } else {
            defined_entry_count = (Integer)args[0];
        }
        final int entryCnt = defined_entry_count;
        logger.info("entryCnt-->" + entryCnt);
        try {
            Connection conn = DBConnection.generateConnection(connect_sqlServer);
            try (PreparedStatement st = conn.prepareStatement(script)) {
                try (ResultSet res = st.executeQuery()) {
                    int cnt = 0;
                    SalesPersonManager salesPersonManager;
                    while (cnt < entryCnt && res.next()) {
                        salesPersonManager = revealSalesPersonManager(res);
                        clo.apply(salesPersonManager.key(), salesPersonManager);
                        cnt++;
                    }
                }
            }
            DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load values from cache store.", e);
        }
        logger.info("warm up finished");
    }

    private SalesPersonManager revealSalesPersonManager(ResultSet res) throws SQLException {
        SalesPersonManager salesPersonManager = new SalesPersonManager(res.getString(1).toLowerCase(), res.getString(2));
        //
        if (LdapUser.MAP.asMap().get(salesPersonManager.getSalesPerson()) == null) {
            LdapUser.MAP.asMap().put(salesPersonManager.getSalesPerson(), Ldap.getLdapUser(salesPersonManager.getSalesPerson(),true));
        }
        salesPersonManager.setSalesPersonName(LdapUser.MAP.asMap().get(salesPersonManager.getSalesPerson()));
        //
        if (salesPersonManager.getManager().equals("NULL")) { //ROOT
            salesPersonManager.setManager(salesPersonManager.getSalesPerson());
            salesPersonManager.setManagerName(salesPersonManager.getSalesPersonName());
        } else {
            if (LdapUser.MAP.asMap().get(salesPersonManager.getManager()) == null) {
                LdapUser.MAP.asMap().put(salesPersonManager.getManager(), Ldap.getLdapUser(salesPersonManager.getManager(),true));
            }
            salesPersonManager.setManager(salesPersonManager.getManager().toLowerCase());
            salesPersonManager.setManagerName(LdapUser.MAP.asMap().get(salesPersonManager.getManager()));
        }
        return salesPersonManager;
    }
}
