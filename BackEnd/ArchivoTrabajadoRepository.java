package com.logismic.iscam.repository;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("unused")
@Service
public class ArchivoTrabajadoRepository {

    @Value("${secundary.database}")
    private String dbo;

    @Value("${secundary.database}.dbo.${worked-files.table}")
    private String table;

    @Value("${worked-files.files.formatfile}")
    private String formatfile;

    @PersistenceContext
    private EntityManager entityManager;

    public ArchivoTrabajadoRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Boolean deleteAllByClaveInformanteAndPeriodoAcronimo(String claveIscam, String periodoAcronimo) {
        getSession().createSQLQuery( "DELETE FROM " + table + " WHERE FKInformante = :claveIscam AND Periodo = :periodoAcronimo")
            .setParameter("claveIscam", claveIscam)
            .setParameter("periodoAcronimo", periodoAcronimo)
            .executeUpdate();
        return true;
    }

    @Transactional
    public Boolean existByIdInformanteAndPeriodoAcronimo(Long idInformante, String periodoAcronimo) {
        Query query = getSession().createSQLQuery( "SELECT FKInformante, Periodo FROM " + table + " WHERE FKInfoarmante = :idInformante AND Periodo = :periodoAcronimo")
            .setParameter("idInformante", idInformante)
            .setParameter("periodoAcronimo", periodoAcronimo);
        List results = query.getResultList();
        return !results.isEmpty();
    }

    @Transactional
    public Boolean bulkInsert(File data) throws SQLException {
        Query query = getSession().createSQLQuery(
            "BULK INSERT " + table +
                " from \'" + data.getAbsolutePath() + "\' WITH(" +
                "KEEPNULLS, " +
                "FORMATFILE = \'" + formatfile + "\')");
        query.executeUpdate();
        return true;
    }

    @Transactional
    public Integer procedureCall(Integer FKInformante, Integer FKPeriodo, Integer TipoCarga) {
        final String spu = "spu_Proceso_General";
        NativeQuery query = getSession()
            .createSQLQuery("EXECUTE " + dbo + ".dbo."+ spu +
                " :FKInformante, :FKPeriodo, :TipoCarga, :TipoReemplazo")
            .setParameter("FKInformante", FKInformante)
            .setParameter("FKPeriodo", FKPeriodo)
            .setParameter("TipoCarga", TipoCarga)
            .setParameter("TipoReemplazo", 0);
        List results = query.list();
        return (Integer) results.get(0);
    }

    @Transactional
    public Integer changeStatus(Integer FKInformante, Integer FKPeriodo, Integer TipoCarga) {
        final String spu = "spu_Proceso_General";
        NativeQuery query = getSession()
            .createSQLQuery("EXECUTE " + dbo + ".dbo."+ spu +
                " :FKInformante, :FKPeriodo, :TipoCarga, :TipoReemplazo")
            .setParameter("FKInformante", FKInformante)
            .setParameter("FKPeriodo", FKPeriodo)
            .setParameter("TipoCarga", TipoCarga)
            .setParameter("TipoReemplazo", 0);
        List results = query.list();
        return (Integer) results.get(0);
    }

    @Transactional
    public String procedureCall(Integer FKInformante, Integer FKPeriodo, Integer TipoCarga, Integer TipoReemplazo) {
        final String spu = "spu_Proceso_General";
        NativeQuery query = getSession()
            .createSQLQuery("EXECUTE " + dbo + ".dbo."+ spu +
                " :FKInformante, :FKPeriodo, :TipoCarga, :TipoReemplazo")
            .setParameter("FKInformante", FKInformante)
            .setParameter("FKPeriodo", FKPeriodo)
            .setParameter("TipoCarga", TipoCarga)
            .setParameter("TipoReemplazo", TipoReemplazo);
        List results = query.list();
        return (String) results.get(0);
    }

    @Transactional
    public Integer procesoGeneral(Integer FKInformante, Integer FKPeriodo, Integer TipoCarga, Integer TipoReemplazo) {
        final String spu = "sp_Proceso_General";
        StoredProcedureQuery query = getSession()
            .createStoredProcedureQuery(spu);
        query.registerStoredProcedureParameter(0, FKInformante.getClass(), ParameterMode.IN);
        query.registerStoredProcedureParameter(1, FKPeriodo.getClass(), ParameterMode.IN);
        query.registerStoredProcedureParameter(2, TipoCarga.getClass(), ParameterMode.IN);
        query.registerStoredProcedureParameter(3, TipoReemplazo.getClass(), ParameterMode.IN);
        query
            .setParameter(0, FKInformante)
            .setParameter(1, FKPeriodo)
            .setParameter(2, TipoCarga)
            .setParameter(3, TipoReemplazo);

        query.registerStoredProcedureParameter(4, Integer.TYPE, ParameterMode.OUT);
        return (Integer) query.getOutputParameterValue(4);
//
//        NativeQuery query = getSession()
//            .createSQLQuery("EXECUTE " + dbo + ".dbo."+ spu +
//                " :FKInformante, :FKPeriodo, :TipoCarga, :TipoReemplazo")
//            .setParameter("FKInformante", FKInformante)
//            .setParameter("FKPeriodo", FKPeriodo)
//            .setParameter("TipoCarga", TipoCarga)
//            .setParameter("TipoReemplazo", TipoReemplazo);
//        List results = query.list();
//        return (Integer) results.get(0);
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
