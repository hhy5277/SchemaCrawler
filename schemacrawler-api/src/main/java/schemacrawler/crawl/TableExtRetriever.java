/*
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2014, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.crawl;


import static sf.util.DatabaseUtility.executeSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schema.ActionOrientationType;
import schemacrawler.schema.CheckOptionType;
import schemacrawler.schema.Column;
import schemacrawler.schema.ConditionTimingType;
import schemacrawler.schema.EventManipulationType;
import schemacrawler.schema.SchemaReference;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.schemacrawler.InformationSchemaViews;

/**
 * A retriever uses database metadata to get the extended details about
 * the database tables.
 *
 * @author Sualeh Fatehi
 */
final class TableExtRetriever
  extends AbstractRetriever
{

  private static final Logger LOGGER = Logger.getLogger(TableExtRetriever.class
    .getName());

  TableExtRetriever(final RetrieverConnection retrieverConnection,
                    final MutableDatabase database)
    throws SQLException
  {
    super(retrieverConnection, database);
  }

  /**
   * Retrieves additional column attributes from the database.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveAdditionalColumnAttributes()
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();
    if (!informationSchemaViews.hasAdditionalColumnAttributesSql())
    {
      LOGGER.log(Level.FINE,
                 "Additional column attributes SQL statement was not provided");
      return;
    }
    final String columnAttributesSql = informationSchemaViews
      .getAdditionalColumnAttributesSql();

    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           columnAttributesSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("TABLE_CATALOG"));
        final String schemaName = quotedName(results.getString("TABLE_SCHEMA"));
        final String tableName = quotedName(results.getString("TABLE_NAME"));
        final String columnName = quotedName(results.getString("COLUMN_NAME"));
        LOGGER.log(Level.FINER, "Retrieving additional column attributes: "
                                + columnName);

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               tableName));
          continue;
        }

        final MutableColumn column = table.getColumn(columnName);
        if (column == null)
        {
          LOGGER.log(Level.FINE, String
            .format("Cannot find column, %s.%s.%s.%s",
                    catalogName,
                    schemaName,
                    tableName,
                    columnName));
          continue;
        }

        column.addAttributes(results.getAttributes());
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING,
                 "Could not retrieve additional column attributes",
                 e);
    }

  }

  /**
   * Retrieves additional table attributes from the database.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveAdditionalTableAttributes()
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();
    if (!informationSchemaViews.hasAdditionalTableAttributesSql())
    {
      LOGGER.log(Level.FINE,
                 "Additional table attributes SQL statement was not provided");
      return;
    }
    final String tableAttributesSql = informationSchemaViews
      .getAdditionalTableAttributesSql();

    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           tableAttributesSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("TABLE_CATALOG"));
        final String schemaName = quotedName(results.getString("TABLE_SCHEMA"));
        final String tableName = quotedName(results.getString("TABLE_NAME"));
        LOGGER.log(Level.FINER, "Retrieving additional table attributes: "
                                + tableName);

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               tableName));
          continue;
        }

        table.addAttributes(results.getAttributes());
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING,
                 "Could not retrieve additional table attributes",
                 e);
    }

  }

  /**
   * Retrieves index information from the database, in the
   * INFORMATION_SCHEMA format.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveIndexInformation()
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();

    if (!informationSchemaViews.hasExtIndexesSql())
    {
      LOGGER.log(Level.FINE,
                 "Indexes information SQL statement was not provided");
      return;
    }
    final String extIndexesInformationSql = informationSchemaViews
      .getExtIndexesSql();

    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           extIndexesInformationSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("INDEX_CATALOG"));
        final String schemaName = quotedName(results.getString("INDEX_SCHEMA"));
        final String tableName = quotedName(results.getString("TABLE_NAME"));
        final String indexName = quotedName(results.getString("INDEX_NAME"));

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               indexName));
          continue;
        }

        LOGGER.log(Level.FINER, "Retrieving index information: " + indexName);
        final MutableIndex index = table.getIndex(indexName);
        if (index == null)
        {
          LOGGER.log(Level.FINE, String
            .format("Cannot find index, %s.%s.%s.%s",
                    catalogName,
                    schemaName,
                    tableName,
                    indexName));
          continue;
        }

        final String definition = results.getString("INDEX_DEFINITION");

        index.appendDefinition(definition);

        index.addAttributes(results.getAttributes());
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve index information", e);
    }

  }

  void retrieveTableColumnPrivileges()
    throws SQLException
  {
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
      .getColumnPrivileges(null, null, "%", "%"));)
    {
      createPrivileges(results, true);
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve table column privileges:"
                                + e.getMessage());
    }
  }

  /**
   * Retrieves table constraint information from the database, in the
   * INFORMATION_SCHEMA format.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveTableConstraintInformation()
    throws SQLException
  {
    final Map<String, MutableTableConstraint> tableConstraintsMap = new HashMap<>();

    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();

    final Connection connection = getDatabaseConnection();

    createTableConstraints(connection,
                           tableConstraintsMap,
                           informationSchemaViews);

    if (!tableConstraintsMap.isEmpty())
    {
      getTableConstraintsColumns(connection,
                                 tableConstraintsMap,
                                 informationSchemaViews);

      getTableConstraintsDefinitions(connection,
                                     tableConstraintsMap,
                                     informationSchemaViews);
    }
  }

  /**
   * Retrieves table definitions from the database, in the
   * INFORMATION_SCHEMA format.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveTableDefinitions()
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();

    if (!informationSchemaViews.hasExtTablesSql())
    {
      LOGGER
        .log(Level.FINE, "Table definitions SQL statement was not provided");
      return;
    }
    final String tableDefinitionsInformationSql = informationSchemaViews
      .getExtTablesSql();

    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           tableDefinitionsInformationSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("TABLE_CATALOG"));
        final String schemaName = quotedName(results.getString("TABLE_SCHEMA"));
        final String tableName = quotedName(results.getString("TABLE_NAME"));

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               tableName));
          continue;
        }

        LOGGER.log(Level.FINER, "Retrieving table information: " + tableName);
        final String definition = results.getString("TABLE_DEFINITION");

        table.appendDefinition(definition);

        table.addAttributes(results.getAttributes());
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve views", e);
    }

  }

  void retrieveTablePrivileges()
    throws SQLException
  {
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
      .getTablePrivileges(null, null, "%"));)
    {
      createPrivileges(results, false);
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve table privileges", e);
    }
  }

  /**
   * Retrieves a trigger information from the database, in the
   * INFORMATION_SCHEMA format.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveTriggerInformation()
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();
    if (!informationSchemaViews.hasTriggerSql())
    {
      LOGGER.log(Level.FINE,
                 "Trigger definition SQL statement was not provided");
      return;
    }
    final String triggerInformationSql = informationSchemaViews
      .getTriggersSql();

    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           triggerInformationSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("TRIGGER_CATALOG"));
        final String schemaName = quotedName(results
          .getString("TRIGGER_SCHEMA"));
        final String triggerName = quotedName(results.getString("TRIGGER_NAME"));
        LOGGER.log(Level.FINER, "Retrieving trigger: " + triggerName);

        // "EVENT_OBJECT_CATALOG", "EVENT_OBJECT_SCHEMA"
        final String tableName = results.getString("EVENT_OBJECT_TABLE");

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               tableName));
          continue;
        }

        final EventManipulationType eventManipulationType = results
          .getEnum("EVENT_MANIPULATION", EventManipulationType.unknown);
        final int actionOrder = results.getInt("ACTION_ORDER", 0);
        final String actionCondition = results.getString("ACTION_CONDITION");
        final String actionStatement = results.getString("ACTION_STATEMENT");
        final ActionOrientationType actionOrientation = results
          .getEnum("ACTION_ORIENTATION", ActionOrientationType.unknown);
        String conditionTimingString = results.getString("ACTION_TIMING");
        if (conditionTimingString == null)
        {
          conditionTimingString = results.getString("CONDITION_TIMING");
        }
        final ConditionTimingType conditionTiming = ConditionTimingType
          .valueOfFromValue(conditionTimingString);

        MutableTrigger trigger = table.lookupTrigger(triggerName);
        if (trigger == null)
        {
          trigger = new MutableTrigger(table, triggerName);
        }
        trigger.setEventManipulationType(eventManipulationType);
        trigger.setActionOrder(actionOrder);
        trigger.appendActionCondition(actionCondition);
        trigger.appendActionStatement(actionStatement);
        trigger.setActionOrientation(actionOrientation);
        trigger.setConditionTiming(conditionTiming);

        trigger.addAttributes(results.getAttributes());
        // Add trigger to the table
        table.addTrigger(trigger);

      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve triggers", e);
    }

  }

  /**
   * Retrieves view information from the database, in the
   * INFORMATION_SCHEMA format.
   *
   * @throws SQLException
   *         On a SQL exception
   */
  void retrieveViewInformation()
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();

    if (!informationSchemaViews.hasViewsSql())
    {
      LOGGER.log(Level.FINE, "Views SQL statement was not provided");
      return;
    }
    final String viewInformationSql = informationSchemaViews.getViewsSql();

    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           viewInformationSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("TABLE_CATALOG"));
        final String schemaName = quotedName(results.getString("TABLE_SCHEMA"));
        final String viewName = quotedName(results.getString("TABLE_NAME"));

        final MutableView view = (MutableView) lookupTable(catalogName,
                                                           schemaName,
                                                           viewName);
        if (view == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               viewName));
          continue;
        }

        LOGGER.log(Level.FINER, "Retrieving view information: " + viewName);
        final String definition = results.getString("VIEW_DEFINITION");
        final CheckOptionType checkOption = results
          .getEnum("CHECK_OPTION", CheckOptionType.unknown);
        final boolean updatable = results.getBoolean("IS_UPDATABLE");

        view.appendDefinition(definition);
        view.setCheckOption(checkOption);
        view.setUpdatable(updatable);

        view.addAttributes(results.getAttributes());
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve views", e);
    }

  }

  private void createPrivileges(final MetadataResultSet results,
                                final boolean privilegesForColumn)
    throws SQLException
  {
    while (results.next())
    {
      final String catalogName = quotedName(results.getString("TABLE_CAT"));
      final String schemaName = quotedName(results.getString("TABLE_SCHEM"));
      final String tableName = quotedName(results.getString("TABLE_NAME"));
      final String columnName;
      if (privilegesForColumn)
      {
        columnName = quotedName(results.getString("COLUMN_NAME"));
      }
      else
      {
        columnName = null;
      }

      final MutableTable table = lookupTable(catalogName, schemaName, tableName);
      if (table == null)
      {
        continue;
      }

      final MutableColumn column = table.getColumn(columnName);
      if (privilegesForColumn && column == null)
      {
        continue;
      }

      final String privilegeName = results.getString("PRIVILEGE");
      final String grantor = results.getString("GRANTOR");
      final String grantee = results.getString("GRANTEE");
      final boolean isGrantable = results.getBoolean("IS_GRANTABLE");

      final MutablePrivilege<?> privilege;
      if (privilegesForColumn)
      {
        final MutablePrivilege<Column> columnPrivilege = column
          .getPrivilege(privilegeName);
        if (columnPrivilege == null)
        {
          privilege = new MutablePrivilege<>(column, privilegeName);
          column.addPrivilege((MutablePrivilege<Column>) privilege);
        }
        else
        {
          privilege = columnPrivilege;
        }
      }
      else
      {
        final MutablePrivilege<Table> tablePrivilege = table
          .getPrivilege(privilegeName);
        if (tablePrivilege == null)
        {
          privilege = new MutablePrivilege<>(table, privilegeName);
          table.addPrivilege((MutablePrivilege<Table>) privilege);
        }
        else
        {
          privilege = tablePrivilege;
        }
      }
      privilege.addGrant(grantor, grantee, isGrantable);

      if (privilegesForColumn)
      {
        column.addPrivilege((MutablePrivilege<Column>) privilege);
      }
      else
      {
        table.addPrivilege((MutablePrivilege<Table>) privilege);
      }
    }
  }

  private void createTableConstraints(final Connection connection,
                                      final Map<String, MutableTableConstraint> tableConstraintsMap,
                                      final InformationSchemaViews informationSchemaViews)
  {
    if (!informationSchemaViews.hasTableConstraintsSql())
    {
      LOGGER
        .log(Level.FINE, "Table constraints SQL statement was not provided");
      return;
    }

    final String tableConstraintsInformationSql = informationSchemaViews
      .getTableConstraintsSql();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           tableConstraintsInformationSql));)
    {

      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("CONSTRAINT_CATALOG"));
        final String schemaName = quotedName(results
          .getString("CONSTRAINT_SCHEMA"));
        final String constraintName = quotedName(results
          .getString("CONSTRAINT_NAME"));
        LOGGER.log(Level.FINER, "Retrieving constraint: " + constraintName);
        // "TABLE_CATALOG", "TABLE_SCHEMA"
        final String tableName = quotedName(results.getString("TABLE_NAME"));

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               tableName));
          continue;
        }

        final String constraintType = results.getString("CONSTRAINT_TYPE");
        final boolean deferrable = results.getBoolean("IS_DEFERRABLE");
        final boolean initiallyDeferred = results
          .getBoolean("INITIALLY_DEFERRED");

        final MutableTableConstraint tableConstraint = new MutableTableConstraint(table,
                                                                                  constraintName);
        tableConstraint.setTableConstraintType(TableConstraintType
          .valueOfFromValue(constraintType));
        tableConstraint.setDeferrable(deferrable);
        tableConstraint.setInitiallyDeferred(initiallyDeferred);

        tableConstraint.addAttributes(results.getAttributes());

        // Add constraint to table
        table.addTableConstraint(tableConstraint);

        // Add to map, since we will need this later
        final String constraintKey = table.getSchema().getFullName() + "."
                                     + constraintName;
        tableConstraintsMap.put(constraintKey, tableConstraint);
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING,
                 "Could not retrieve table constraint information",
                 e);
      return;
    }
  }

  private void getTableConstraintsColumns(final Connection connection,
                                          final Map<String, MutableTableConstraint> tableConstraintsMap,
                                          final InformationSchemaViews informationSchemaViews)
  {
    if (!informationSchemaViews.hasTableConstraintsColumnsSql())
    {
      LOGGER
        .log(Level.FINE,
             "Extended table constraints columns SQL statement was not provided");
      return;
    }
    final String tableConstraintsColumnsInformationSql = informationSchemaViews
      .getTableConstraintsColumnsSql();

    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           tableConstraintsColumnsInformationSql));)
    {
      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("CONSTRAINT_CATALOG"));
        final String schemaName = quotedName(results
          .getString("CONSTRAINT_SCHEMA"));
        final String constraintName = quotedName(results
          .getString("CONSTRAINT_NAME"));
        LOGGER.log(Level.FINER, "Retrieving constraint definition: "
                                + constraintName);

        final String constraintKey = new SchemaReference(catalogName,
                                                         schemaName)
                                     + "."
                                     + constraintName;
        final MutableTableConstraint tableConstraint = tableConstraintsMap
          .get(constraintKey);
        if (tableConstraint == null)
        {
          LOGGER.log(Level.FINEST,
                     "Could not add column for constraint to table: "
                         + constraintName);
          continue;
        }

        // "TABLE_CATALOG", "TABLE_SCHEMA"
        final String tableName = quotedName(results.getString("TABLE_NAME"));

        final MutableTable table = lookupTable(catalogName,
                                               schemaName,
                                               tableName);
        if (table == null)
        {
          LOGGER.log(Level.FINE, String.format("Cannot find table, %s.%s.%s",
                                               catalogName,
                                               schemaName,
                                               tableName));
          continue;
        }

        final String columnName = quotedName(results.getString("COLUMN_NAME"));
        final MutableColumn column = table.getColumn(columnName);
        if (column == null)
        {
          LOGGER.log(Level.FINE, String
            .format("Cannot find column, %s.%s.%s.%s",
                    catalogName,
                    schemaName,
                    tableName,
                    columnName));
          continue;
        }
        final int ordinalPosition = results.getInt("ORDINAL_POSITION", 0);
        final MutableTableConstraintColumn constraintColumn = new MutableTableConstraintColumn(tableConstraint,
                                                                                               column);
        constraintColumn.setTableConstraintOrdinalPosition(ordinalPosition);

        tableConstraint.addColumn(constraintColumn);
      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve check constraints", e);
    }
  }

  private void getTableConstraintsDefinitions(final Connection connection,
                                              final Map<String, MutableTableConstraint> tableConstraintsMap,
                                              final InformationSchemaViews informationSchemaViews)
  {
    if (!informationSchemaViews.hasExtTableConstraintsSql())
    {
      LOGGER.log(Level.FINE,
                 "Extended table constraints SQL statement was not provided");
      return;
    }
    final String extTableConstraintInformationSql = informationSchemaViews
      .getExtTableConstraintsSql();

    // Get check constraint definitions
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(executeSql(statement,
                                                                           extTableConstraintInformationSql));)
    {
      while (results.next())
      {
        final String catalogName = quotedName(results
          .getString("CONSTRAINT_CATALOG"));
        final String schemaName = quotedName(results
          .getString("CONSTRAINT_SCHEMA"));
        final String constraintName = quotedName(results
          .getString("CONSTRAINT_NAME"));
        LOGGER.log(Level.FINER, "Retrieving constraint definition: "
                                + constraintName);
        final String definition = results.getString("CHECK_CLAUSE");

        final String constraintKey = new SchemaReference(catalogName,
                                                         schemaName)
                                     + "."
                                     + constraintName;
        final MutableTableConstraint tableConstraint = tableConstraintsMap
          .get(constraintKey);
        if (tableConstraint == null)
        {
          LOGGER.log(Level.FINEST,
                     "Could not add constraint definition to table: "
                         + constraintName);
          continue;
        }
        tableConstraint.appendDefinition(definition);

        tableConstraint.addAttributes(results.getAttributes());

      }
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve check constraints", e);
    }
  }

}
