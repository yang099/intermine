package org.intermine.objectstore.intermine;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import junit.framework.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.intermine.metadata.Model;
import org.intermine.objectstore.Failure;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.SetupDataTestCase;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryExpression;
import org.intermine.objectstore.query.QueryFunction;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.sql.Database;
import org.intermine.sql.DatabaseFactory;
import org.intermine.testing.OneTimeTestCase;
import org.intermine.util.TypeUtil;

import org.intermine.model.testmodel.*;

public class SqlGeneratorTest extends SetupDataTestCase
{
    protected static Database db;
    protected static HashMap results2 = new HashMap();

    public SqlGeneratorTest(String arg) {
        super(arg);
    }

    public static Test suite() {
        return OneTimeTestCase.buildSuite(SqlGeneratorTest.class);
    }

    public static void oneTimeSetUp() throws Exception {
        SetupDataTestCase.oneTimeSetUp();
        setUpResults();
        db = DatabaseFactory.getDatabase("db.unittest");
    }

    public static void setUpResults() throws Exception {
        results.put("SelectSimpleObject", "SELECT intermine_Alias.OBJECT AS \"intermine_Alias\", intermine_Alias.id AS \"intermine_Aliasid\" FROM Company AS intermine_Alias ORDER BY intermine_Alias.id");
        results2.put("SelectSimpleObject", Collections.singleton("Company"));
        results.put("SubQuery", "SELECT DISTINCT intermine_All.intermine_Arrayname AS a1_, intermine_All.intermine_Alias AS \"intermine_Alias\" FROM (SELECT DISTINCT intermine_Array.OBJECT AS intermine_Array, intermine_Array.CEOId AS intermine_ArrayCEOId, intermine_Array.addressId AS intermine_ArrayaddressId, intermine_Array.id AS intermine_Arrayid, intermine_Array.name AS intermine_Arrayname, intermine_Array.vatNumber AS intermine_ArrayvatNumber, 5 AS intermine_Alias FROM Company AS intermine_Array) AS intermine_All ORDER BY intermine_All.intermine_Arrayname, intermine_All.intermine_Alias");
        results2.put("SubQuery", Collections.singleton("Company"));
        results.put("WhereSimpleEquals", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.vatNumber = 1234 ORDER BY a1_.name");
        results2.put("WhereSimpleEquals", Collections.singleton("Company"));
        results.put("WhereSimpleNotEquals", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.vatNumber != 1234 ORDER BY a1_.name");
        results2.put("WhereSimpleNotEquals", Collections.singleton("Company"));
        results.put("WhereSimpleNegEquals", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.vatNumber != 1234 ORDER BY a1_.name");
        results2.put("WhereSimpleNegEquals", Collections.singleton("Company"));
        results.put("WhereSimpleLike", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.name LIKE 'Company%' ORDER BY a1_.name");
        results2.put("WhereSimpleLike", Collections.singleton("Company"));
        results.put("WhereEqualsString", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE a1_.name = 'CompanyA' ORDER BY a1_.name");
        results2.put("WhereEqualsString", Collections.singleton("Company"));
        results.put("WhereAndSet", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000) ORDER BY a1_.name");
        results2.put("WhereAndSet", Collections.singleton("Company"));
        results.put("WhereOrSet", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE (a1_.name LIKE 'CompanyA%' OR a1_.vatNumber > 2000) ORDER BY a1_.name");
        results2.put("WhereOrSet", Collections.singleton("Company"));
        results.put("WhereNotSet", "SELECT DISTINCT a1_.name AS a2_ FROM Company AS a1_ WHERE ( NOT (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000)) ORDER BY a1_.name");
        results2.put("WhereNotSet", Collections.singleton("Company"));
        results.put("WhereSubQueryField", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.name AS orderbyfield0 FROM Department AS a1_ WHERE a1_.name IN (SELECT DISTINCT a1_.name FROM Department AS a1_) ORDER BY a1_.name, a1_.id");
        results2.put("WhereSubQueryField", Collections.singleton("Department"));
        results.put("WhereSubQueryClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id IN (SELECT DISTINCT a1_.id FROM Company AS a1_ WHERE a1_.name = 'CompanyA') ORDER BY a1_.id");
        results2.put("WhereSubQueryClass", Collections.singleton("Company"));
        results.put("WhereNotSubQueryClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id NOT IN (SELECT DISTINCT a1_.id FROM Company AS a1_ WHERE a1_.name = 'CompanyA') ORDER BY a1_.id");
        results2.put("WhereNotSubQueryClass", Collections.singleton("Company"));
        results.put("WhereNegSubQueryClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id NOT IN (SELECT DISTINCT a1_.id FROM Company AS a1_ WHERE a1_.name = 'CompanyA') ORDER BY a1_.id");
        results2.put("WhereNegSubQueryClass", Collections.singleton("Company"));
        results.put("WhereClassClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Company AS a2_ WHERE a1_.id = a2_.id ORDER BY a1_.id, a2_.id");
        results2.put("WhereClassClass", Collections.singleton("Company"));
        results.put("WhereNotClassClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Company AS a2_ WHERE a1_.id != a2_.id ORDER BY a1_.id, a2_.id");
        results2.put("WhereNotClassClass", Collections.singleton("Company"));
        results.put("WhereNegClassClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Company AS a2_ WHERE a1_.id != a2_.id ORDER BY a1_.id, a2_.id");
        results2.put("WhereNegClassClass", Collections.singleton("Company"));
        Integer id1 = (Integer) TypeUtil.getFieldValue(data.get("CompanyA"), "id");
        results.put("WhereClassObject", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE a1_.id = " + id1 + " ORDER BY a1_.id");
        results2.put("WhereClassObject", Collections.singleton("Company"));
        results.put("Contains11", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Manager AS a2_ WHERE (a1_.managerId = a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results2.put("Contains11", new HashSet(Arrays.asList(new String[] {"Department", "Manager"})));
        results.put("ContainsNot11", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Manager AS a2_ WHERE (a1_.managerId != a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsNot11", new HashSet(Arrays.asList(new String[] {"Department", "Manager"})));
        results.put("ContainsNeg11", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Manager AS a2_ WHERE (a1_.managerId != a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsNeg11", new HashSet(Arrays.asList(new String[] {"Department", "Manager"})));
        results.put("Contains1N", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Department AS a2_ WHERE (a1_.id = a2_.companyId AND a1_.name = 'CompanyA') ORDER BY a1_.id, a2_.id");
        results2.put("Contains1N", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("ContainsN1", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Department AS a1_, Company AS a2_ WHERE (a1_.companyId = a2_.id AND a2_.name = 'CompanyA') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsN1", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("ContainsMN", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Contractor AS a1_, Company AS a2_, CompanysContractors AS indirect0 WHERE ((a1_.id = indirect0.Companys AND indirect0.Contractors = a2_.id) AND a1_.name = 'ContractorA') ORDER BY a1_.id, a2_.id");
        results2.put("ContainsMN", new HashSet(Arrays.asList(new String[] {"Contractor", "Company", "CompanysContractors"})));
        results.put("ContainsDuplicatesMN", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Contractor AS a1_, Company AS a2_, OldComsOldContracts AS indirect0 WHERE (a1_.id = indirect0.OldComs AND indirect0.OldContracts = a2_.id) ORDER BY a1_.id, a2_.id");
        results2.put("ContainsDuplicatesMN", new HashSet(Arrays.asList(new String[] {"Contractor", "Company", "OldComsOldContracts"})));
        id1 = (Integer) TypeUtil.getFieldValue(data.get("EmployeeA1"), "id");
        results.put("ContainsObject", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Department AS a1_ WHERE a1_.managerId = " + id1 + " ORDER BY a1_.id");
        results2.put("ContainsObject", Collections.singleton("Department"));
        results.put("SimpleGroupBy", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, COUNT(*) AS a2_ FROM Company AS a1_, Department AS a3_ WHERE a1_.id = a3_.companyId GROUP BY a1_.OBJECT, a1_.CEOId, a1_.addressId, a1_.id, a1_.name, a1_.vatNumber ORDER BY a1_.id, COUNT(*)");
        results2.put("SimpleGroupBy", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("MultiJoin", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id, a4_.OBJECT AS a4_, a4_.id AS a4_id FROM Company AS a1_, Department AS a2_, Manager AS a3_, Address AS a4_ WHERE (a1_.id = a2_.companyId AND a2_.managerId = a3_.id AND a3_.addressId = a4_.id AND a3_.name = 'EmployeeA1') ORDER BY a1_.id, a2_.id, a3_.id, a4_.id");
        results2.put("MultiJoin", new HashSet(Arrays.asList(new String[] {"Department", "Manager", "Company", "Address"})));
        results.put("SelectComplex", "SELECT DISTINCT (AVG(a1_.vatNumber) + 20) AS a3_, a2_.name AS a4_, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Department AS a2_ GROUP BY a2_.OBJECT, a2_.companyId, a2_.id, a2_.managerId, a2_.name ORDER BY (AVG(a1_.vatNumber) + 20), a2_.name, a2_.id");
        results2.put("SelectComplex", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("SelectClassAndSubClasses", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.name AS orderbyfield0 FROM Employee AS a1_ ORDER BY a1_.name, a1_.id");
        results2.put("SelectClassAndSubClasses", Collections.singleton("Employee"));
        results.put("SelectInterfaceAndSubClasses", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_ ORDER BY a1_.id");
        results2.put("SelectInterfaceAndSubClasses", Collections.singleton("Employable"));
        results.put("SelectInterfaceAndSubClasses2", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM RandomInterface AS a1_ ORDER BY a1_.id");
        results2.put("SelectInterfaceAndSubClasses2", Collections.singleton("RandomInterface"));
        results.put("SelectInterfaceAndSubClasses3", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM ImportantPerson AS a1_ ORDER BY a1_.id");
        results2.put("SelectInterfaceAndSubClasses3", Collections.singleton("ImportantPerson"));
        results.put("OrderByAnomaly", "SELECT DISTINCT 5 AS a2_, a1_.name AS a3_ FROM Company AS a1_ ORDER BY a1_.name");
        results2.put("OrderByAnomaly", Collections.singleton("Company"));
        Integer id2 = (Integer) TypeUtil.getFieldValue(data.get("CompanyA"), "id");
        Integer id3 = (Integer) TypeUtil.getFieldValue(data.get("DepartmentA1"), "id");
        results.put("SelectClassObjectSubquery", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_, Department AS a2_ WHERE (a1_.id = " + id2 + " AND a1_.id = a2_.companyId AND a2_.id IN (SELECT DISTINCT a1_.id FROM Department AS a1_ WHERE a1_.id = " + id3 + ")) ORDER BY a1_.id");
        results2.put("SelectClassObjectSubquery", new HashSet(Arrays.asList(new String[] {"Department", "Company"})));
        results.put("SelectUnidirectionalCollection", "SELECT DISTINCT a2_.OBJECT AS a2_, a2_.id AS a2_id FROM Company AS a1_, Secretary AS a2_, HasSecretarysSecretarys AS indirect0 WHERE (a1_.name = 'CompanyA' AND (a1_.id = indirect0.Secretarys AND indirect0.HasSecretarys = a2_.id)) ORDER BY a2_.id");
        results2.put("SelectUnidirectionalCollection", new HashSet(Arrays.asList(new String[] {"Company", "Secretary", "HasSecretarysSecretarys"})));
        results.put("EmptyAndConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE true ORDER BY a1_.id");
        results2.put("EmptyAndConstraintSet", Collections.singleton("Company"));
        results.put("EmptyOrConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE false ORDER BY a1_.id");
        results2.put("EmptyOrConstraintSet", Collections.singleton("Company"));
        results.put("EmptyNandConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE false ORDER BY a1_.id");
        results2.put("EmptyNandConstraintSet", Collections.singleton("Company"));
        results.put("EmptyNorConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE true ORDER BY a1_.id");
        results2.put("EmptyNorConstraintSet", Collections.singleton("Company"));
        results.put("BagConstraint", "SELECT DISTINCT Company.OBJECT AS \"Company\", Company.id AS \"Companyid\" FROM Company AS Company WHERE (Company.name IN ('CompanyA', 'goodbye', 'hello')) ORDER BY Company.id");
        results2.put("BagConstraint", Collections.singleton("Company"));
        results.put("BagConstraint2", "SELECT DISTINCT Company.OBJECT AS \"Company\", Company.id AS \"Companyid\" FROM Company AS Company WHERE (Company.id IN (" + id2 + ")) ORDER BY Company.id");
        results2.put("BagConstraint2", Collections.singleton("Company"));
        results.put("InterfaceField", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_ WHERE a1_.name = 'EmployeeA1' ORDER BY a1_.id");
        results2.put("InterfaceField", Collections.singleton("Employable"));
        results.put("InterfaceReference", NO_RESULT);
        results.put("InterfaceCollection", NO_RESULT);
        Set res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1__1.debt AS a2_, a1_.age AS a3_ FROM Employee AS a1_, Broke AS a1__1 WHERE a1_.id = a1__1.id AND (a1__1.debt > 0 AND a1_.age > 0) ORDER BY a1_.id, a1__1.debt, a1_.age");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.debt AS a2_, a1__1.age AS a3_ FROM Broke AS a1_, Employee AS a1__1 WHERE a1_.id = a1__1.id AND (a1_.debt > 0 AND a1__1.age > 0) ORDER BY a1_.id, a1_.debt, a1__1.age");
        results.put("DynamicInterfacesAttribute", res);
        results2.put("DynamicInterfacesAttribute", new HashSet(Arrays.asList(new String[] {"Employee", "Broke"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_, Broke AS a1__1 WHERE a1_.id = a1__1.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Broke AS a1_, Employable AS a1__1 WHERE a1_.id = a1__1.id ORDER BY a1_.id");
        results.put("DynamicClassInterface", res);
        results2.put("DynamicClassInterface", new HashSet(Arrays.asList(new String[] {"Employable", "Broke"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Department AS a1_, Broke AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a2_.id = a1_.companyId AND a3_.id = a1__1.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Broke AS a1_, Department AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a2_.id = a1__1.companyId AND a3_.id = a1_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef1", res);
        results2.put("DynamicClassRef1", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Department AS a1_, Broke AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a1_.companyId = a2_.id AND a1__1.bankId = a3_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Broke AS a1_, Department AS a1__1, Company AS a2_, Bank AS a3_ WHERE a1_.id = a1__1.id AND (a1__1.companyId = a2_.id AND a1_.bankId = a3_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef2", res);
        results2.put("DynamicClassRef2", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Company AS a1_, Bank AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a1_.id = a2_.companyId AND a1_.id = a3_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Bank AS a1_, Company AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a1_.id = a2_.companyId AND a1_.id = a3_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef3", res);
        results2.put("DynamicClassRef3", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Company AS a1_, Bank AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a2_.companyId = a1_.id AND a3_.bankId = a1_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM Bank AS a1_, Company AS a1__1, Department AS a2_, Broke AS a3_ WHERE a1_.id = a1__1.id AND (a2_.companyId = a1_.id AND a3_.bankId = a1_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef4", res);
        results2.put("DynamicClassRef4", new HashSet(Arrays.asList(new String[] {"Department", "Broke", "Company", "Bank"})));
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_, Broke AS a1__1, HasAddress AS a2_, Broke AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employable AS a1_, Broke AS a1__1, Broke AS a2_, HasAddress AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Broke AS a1_, Employable AS a1__1, HasAddress AS a2_, Broke AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Broke AS a1_, Employable AS a1__1, Broke AS a2_, HasAddress AS a2__1 WHERE a1_.id = a1__1.id AND a2_.id = a2__1.id AND a1_.id = a2_.id ORDER BY a1_.id");
        results.put("DynamicClassConstraint", res);
        results2.put("DynamicClassConstraint", new HashSet(Arrays.asList(new String[] {"Employable", "Broke", "HasAddress"})));
        results.put("ContainsConstraintNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE a1_.addressId IS NULL ORDER BY a1_.id");
        results2.put("ContainsConstraintNull", Collections.singleton("Employee"));
        results.put("ContainsConstraintNotNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Employee AS a1_ WHERE a1_.addressId IS NOT NULL ORDER BY a1_.id");
        results2.put("ContainsConstraintNotNull", Collections.singleton("Employee"));
        results.put("SimpleConstraintNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Manager AS a1_ WHERE a1_.title IS NULL ORDER BY a1_.id");
        results2.put("SimpleConstraintNull", Collections.singleton("Manager"));
        results.put("SimpleConstraintNotNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Manager AS a1_ WHERE a1_.title IS NOT NULL ORDER BY a1_.id");
        results2.put("SimpleConstraintNotNull", Collections.singleton("Manager"));
        results.put("TypeCast", "SELECT DISTINCT (a1_.age)::TEXT AS a2_ FROM Employee AS a1_ ORDER BY (a1_.age)::TEXT");
        results2.put("TypeCast", Collections.singleton("Employee"));
        results.put("IndexOf", "SELECT STRPOS(a1_.name, 'oy') AS a2_ FROM Employee AS a1_ ORDER BY STRPOS(a1_.name, 'oy')");
        results2.put("IndexOf", Collections.singleton("Employee"));
        results.put("Substring", "SELECT SUBSTR(a1_.name, 2, 2) AS a2_ FROM Employee AS a1_ ORDER BY SUBSTR(a1_.name, 2, 2)");
        results2.put("Substring", Collections.singleton("Employee"));
        results.put("Substring2", "SELECT SUBSTR(a1_.name, 2) AS a2_ FROM Employee AS a1_ ORDER BY SUBSTR(a1_.name, 2)");
        results2.put("Substring2", Collections.singleton("Employee"));
        results.put("OrderByReference", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.departmentId AS orderbyfield0 FROM Employee AS a1_ ORDER BY a1_.departmentId, a1_.id");
        results2.put("OrderByReference", Collections.singleton("Employee"));
        results.put("FailDistinctOrder", new Failure(ObjectStoreException.class, "Field a1_.age in the ORDER BY list must be in the SELECT list, or the whole QueryClass org.intermine.model.testmodel.Employee must be in the SELECT list, or the query made non-distinct"));
        String largeBagConstraintText = new BufferedReader(new InputStreamReader(TruncatedSqlGeneratorTest.class.getClassLoader().getResourceAsStream("test/largeBag.sql"))).readLine();
        results.put("LargeBagConstraint", largeBagConstraintText);
        results2.put("LargeBagConstraint", Collections.singleton("Employee"));
    }

    public void executeTest(String type) throws Exception {
        Query q = (Query) queries.get(type);
        Object expected = results.get(type);
        if ("LargeBagConstraint".equals(type)) {
            System.out.println("LargeBagConstraint: " + expected);
        }
        if (expected instanceof Failure) {
            try {
                SqlGenerator.generate(q, 0, Integer.MAX_VALUE, getSchema(), db);
                fail(type + " was expected to fail");
            } catch (Exception e) {
                assertEquals(type + " was expected to produce a particular exception", expected, new Failure(e));
            }
        } else {
            String generated = SqlGenerator.generate(q, 0, Integer.MAX_VALUE, getSchema(), db);
            if (expected instanceof String) {
                assertEquals("", results.get(type), generated);
            } else if (expected instanceof Collection) {
                boolean hasEqual = false;
                Iterator expectedIter = ((Collection) expected).iterator();
                while ((!hasEqual) && expectedIter.hasNext()) {
                    String expectedString = (String) expectedIter.next();
                    hasEqual = expectedString.equals(generated);
                }
                assertTrue(generated, hasEqual);
            } else {
                assertTrue("No result found for " + type, false);
            }

            assertEquals(results2.get(type), SqlGenerator.findTableNames(q, getSchema()));

            // TODO: extend sql so that it can represent these
            if (!("TypeCast".equals(type) || "IndexOf".equals(type) || "Substring".equals(type) || "Substring2".equals(type) || type.startsWith("Empty") || type.startsWith("BagConstraint") || "LargeBagConstraint".equals(type))) {
                // And check that the SQL generated is high enough quality to be parsed by the optimiser.
                org.intermine.sql.query.Query sql = new org.intermine.sql.query.Query(generated);
            }
        }
    }

    public void testSelectQueryValue() throws Exception {
        QueryValue v1 = new QueryValue(new Integer(5));
        QueryValue v2 = new QueryValue("Hello");
        QueryValue v3 = new QueryValue(new Date(1046275720000l));
        QueryValue v4 = new QueryValue(Boolean.TRUE);
        StringBuffer buffer = new StringBuffer();
        SqlGenerator.queryEvaluableToString(buffer, v1, null, null);
        SqlGenerator.queryEvaluableToString(buffer, v2, null, null);
        SqlGenerator.queryEvaluableToString(buffer, v3, null, null);
        SqlGenerator.queryEvaluableToString(buffer, v4, null, null);
        assertEquals("5'Hello'1046275720000'true'", buffer.toString());
    }

    public void testSelectQueryExpression() throws Exception {
        QueryValue v1 = new QueryValue(new Integer(5));
        QueryValue v2 = new QueryValue(new Integer(7));
        QueryExpression e1 = new QueryExpression(v1, QueryExpression.ADD, v2);
        QueryExpression e2 = new QueryExpression(v1, QueryExpression.SUBTRACT, v2);
        QueryExpression e3 = new QueryExpression(v1, QueryExpression.MULTIPLY, v2);
        QueryExpression e4 = new QueryExpression(v1, QueryExpression.DIVIDE, v2);
        StringBuffer buffer = new StringBuffer();
        SqlGenerator.queryEvaluableToString(buffer, e1, null, null);
        SqlGenerator.queryEvaluableToString(buffer, e2, null, null);
        SqlGenerator.queryEvaluableToString(buffer, e3, null, null);
        SqlGenerator.queryEvaluableToString(buffer, e4, null, null);
        assertEquals("(5 + 7)(5 - 7)(5 * 7)(5 / 7)", buffer.toString());
    }

    public void testSelectQuerySubstringExpression() throws Exception {
        QueryValue v1 = new QueryValue("Hello");
        QueryValue v2 = new QueryValue(new Integer(3));
        QueryValue v3 = new QueryValue(new Integer(5));
        QueryExpression e1 = new QueryExpression(v1, v2, v3);
        StringBuffer buffer = new StringBuffer();
        SqlGenerator.queryEvaluableToString(buffer, e1, null, null);
        assertEquals("SUBSTR('Hello', 3, 5)", buffer.toString());
    }

    /* TODO
    public void testSelectQueryField() throws Exception {
        QueryClass c1 = new QueryClass(Department.class);
        QueryField f1 = new QueryField(c1, "name");
        Query q1 = new Query();
        q1.addFrom(c1);
        StringBuffer buffer = new StringBuffer();
        SqlGenerator.queryEvaluableToString(buffer, f1, q1);
        assertEquals("a1_.name", buffer.toString());
    }

    public void testSelectQueryFunction() throws Exception {
        QueryClass c1 = new QueryClass(Company.class);
        QueryField v1 = new QueryField(c1, "vatNumber");
        QueryFunction f1 = new QueryFunction();
        QueryFunction f2 = new QueryFunction(v1, QueryFunction.SUM);
        QueryFunction f3 = new QueryFunction(v1, QueryFunction.AVERAGE);
        QueryFunction f4 = new QueryFunction(v1, QueryFunction.MIN);
        QueryFunction f5 = new QueryFunction(v1, QueryFunction.MAX);
        Query q1 = new Query();
        q1.addFrom(c1);
        StringBuffer buffer = new StringBuffer();
        SqlGenerator.queryEvaluableToString(buffer, f1, q1);
        SqlGenerator.queryEvaluableToString(buffer, f2, q1);
        SqlGenerator.queryEvaluableToString(buffer, f3, q1);
        SqlGenerator.queryEvaluableToString(buffer, f4, q1);
        SqlGenerator.queryEvaluableToString(buffer, f5, q1);
        assertEquals("COUNT(*)SUM(a1_.vatNumber)AVG(a1_.vatNumber)MIN(a1_.vatNumber)MAX(a1_.vatNumber)", buffer.toString());
    }
*/

    public void testInvalidClass() throws Exception {
        Query q = new Query();
        QueryClass c1 = new QueryClass(Company.class);
        q.addFrom(c1);
        q.addToSelect(c1);
        try {
            SqlGenerator.generate(q, 0, Integer.MAX_VALUE, new DatabaseSchema(new Model("nothing", "http://www.intermine.org/model/testmodel", new HashSet()), Collections.EMPTY_LIST), db);
            fail("Expected: ObjectStoreException");
        } catch (ObjectStoreException e) {
        }
    }

    public void testRegisterOffset() throws Exception {
        DatabaseSchema schema = getSchema();
        Query q = new Query();
        QueryClass c1 = new QueryClass(Company.class);
        q.addFrom(c1);
        q.addToSelect(c1);
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        SqlGenerator.registerOffset(q, 5, schema, db, new Integer(10));
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 10, Integer.MAX_VALUE, schema, db));
        SqlGenerator.registerOffset(q, 11000, schema, db, new Integer(20));
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 10, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 20 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 11005, Integer.MAX_VALUE, schema, db));
        SqlGenerator.registerOffset(q, 21000, schema, db, new Integer(30));
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 10, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 11000", SqlGenerator.generate(q, 11005, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 30 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 21005, Integer.MAX_VALUE, schema, db));
        SqlGenerator.registerOffset(q, 21005, schema, db, new Integer(31));
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 10, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 11000", SqlGenerator.generate(q, 11005, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 30 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 21005, Integer.MAX_VALUE, schema, db));
        SqlGenerator.registerOffset(q, 11002, schema, db, new Integer(29));
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 10, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 11000", SqlGenerator.generate(q, 11005, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 30 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 21005, Integer.MAX_VALUE, schema, db));
        SqlGenerator.registerOffset(q, 101000, schema, db, new Integer(40));
        assertEquals(getRegisterOffset1(), SqlGenerator.generate(q, 0, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 10, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 11000", SqlGenerator.generate(q, 11005, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 10 ORDER BY a1_.id OFFSET 21000", SqlGenerator.generate(q, 21005, Integer.MAX_VALUE, schema, db));
        assertEquals(getRegisterOffset2() + "a1_.id > 40 ORDER BY a1_.id OFFSET 5", SqlGenerator.generate(q, 101005, Integer.MAX_VALUE, schema, db));
    }

    protected DatabaseSchema getSchema() {
        return new DatabaseSchema(model, Collections.EMPTY_LIST);
    }
    public String getRegisterOffset1() {
        return "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ ORDER BY a1_.id";
    }
    public String getRegisterOffset2() {
        return "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM Company AS a1_ WHERE ";
    }
}

