package uk.nhs.adaptors.connector.config;

import java.util.List;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;

@Configuration
@EntityScan(basePackages = {"uk.nhs.adaptors.connector"})
@ComponentScan(basePackages = {"uk.nhs.adaptors.connector"})
public class DbConnectorConfiguration {

    @Bean
    public Jdbi jdbi(DataSource ds, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers) {
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(ds);
        Jdbi jdbi = Jdbi.create(proxy);
        jdbiPlugins.forEach(jdbi::installPlugin);
        rowMappers.forEach(jdbi::registerRowMapper);
        return jdbi;
    }

    @Bean
    public JdbiPlugin sqlObjectPlugin() {
        return new SqlObjectPlugin();
    }

    @Bean
    public JdbiPlugin postgresPlugin() {
        return new PostgresPlugin();
    }

    @Bean
    public PatientMigrationRequestDao patientMigrationRequestDao(Jdbi jdbi) {
        return jdbi.onDemand(PatientMigrationRequestDao.class);
    }

    @Bean
    public MigrationStatusLogDao migrationStatusLogDao(Jdbi jdbi) {
        return jdbi.onDemand(MigrationStatusLogDao.class);
    }
}