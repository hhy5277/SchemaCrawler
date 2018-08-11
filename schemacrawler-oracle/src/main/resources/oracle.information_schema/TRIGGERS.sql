SELECT /*+ PARALLEL(AUTO) */
  NULL AS TRIGGER_CATALOG,
  TRIGGERS.OWNER AS TRIGGER_SCHEMA,
  TRIGGER_NAME AS TRIGGER_NAME,
  CASE
    WHEN TRIGGERS.TRIGGERING_EVENT LIKE '%INSERT%' THEN 'INSERT'
    WHEN TRIGGERS.TRIGGERING_EVENT LIKE '%UPDATE%'  THEN 'UPDATE'
    WHEN TRIGGERS.TRIGGERING_EVENT LIKE '%DELETE%'  THEN 'DELETE'
    ELSE 'UNKNOWN'
  END
    AS EVENT_MANIPULATION,
  NULL AS EVENT_OBJECT_CATALOG,
  TRIGGERS.TABLE_OWNER AS EVENT_OBJECT_SCHEMA,
  TRIGGERS.TABLE_NAME AS EVENT_OBJECT_TABLE,
  0
    AS ACTION_ORDER,
  TRIGGERS.WHEN_CLAUSE AS ACTION_CONDITION,
  CASE
    WHEN TRIGGERS.TRIGGER_TYPE LIKE '%ROW' THEN 'ROW'
    WHEN TRIGGERS.TRIGGER_TYPE LIKE '%STATEMENT' THEN 'STATEMENT'
    ELSE 'UNKNOWN'
  END
    AS ACTION_ORIENTATION,
  CASE
    WHEN TRIGGERS.TRIGGER_TYPE LIKE 'AFTER%' THEN 'AFTER'
    WHEN TRIGGERS.TRIGGER_TYPE LIKE 'BEFORE%' THEN 'BEFORE'
    ELSE 'INSTEAD OF'
  END
    AS CONDITION_TIMING,
  TRIGGERS.TRIGGER_BODY AS ACTION_STATEMENT
FROM
  ${catalogscope}_TRIGGERS TRIGGERS
WHERE
  TRIGGERS.OWNER NOT IN 
    ('ANONYMOUS', 'APEX_PUBLIC_USER', 'APPQOSSYS', 'BI', 'CTXSYS', 'DBSNMP', 'DIP', 
    'EXFSYS', 'FLOWS_30000', 'FLOWS_FILES', 'GSMADMIN_INTERNAL', 'IX', 'LBACSYS', 
    'MDDATA', 'MDSYS', 'MGMT_VIEW', 'OE', 'OLAPSYS', 'ORACLE_OCM', 
    'ORDPLUGINS', 'ORDSYS', 'OUTLN', 'OWBSYS', 'PM', 'SCOTT', 'SH', 
    'SI_INFORMTN_SCHEMA', 'SPATIAL_CSW_ADMIN_USR', 'SPATIAL_WFS_ADMIN_USR', 
    'SYS', 'SYSMAN', 'SYSTEM', 'TSMSYS', 'WKPROXY', 'WKSYS', 'WK_TEST', 
    'WMSYS', 'XDB', 'XS$NULL', 'RDSADMIN')  
  AND NOT REGEXP_LIKE(TRIGGERS.OWNER, '^APEX_[0-9]{6}$')
  AND NOT REGEXP_LIKE(TRIGGERS.OWNER, '^FLOWS_[0-9]{5,6}$')
  AND REGEXP_LIKE(TRIGGERS.OWNER, '${schemas}')

