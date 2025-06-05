package kitra.awachat.next.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 用于实现 PostgreSQL JSONB 数据与 Map 类型互转的处理器
 */
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(objectMapper.writeValueAsString(parameter));
            ps.setObject(i, jsonObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error serializing Map to JSON string", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        PGobject pgObject = (PGobject) rs.getObject(columnName);
        if (pgObject == null || pgObject.getValue() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(pgObject.getValue(), Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error deserializing JSON string to Map", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        PGobject pgObject = (PGobject) rs.getObject(columnIndex);
        if (pgObject == null || pgObject.getValue() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(pgObject.getValue(), Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error deserializing JSON string to Map", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        PGobject pgObject = (PGobject) cs.getObject(columnIndex);
        if (pgObject == null || pgObject.getValue() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(pgObject.getValue(), Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error deserializing JSON string to Map", e);
        }
    }
}