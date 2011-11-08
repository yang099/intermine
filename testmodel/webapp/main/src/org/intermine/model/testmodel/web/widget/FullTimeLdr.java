package org.intermine.model.testmodel.web.widget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.Factory;

import org.apache.commons.collections.map.LazyMap;

import org.intermine.api.profile.InterMineBag;

import org.intermine.api.query.MainHelper;

import org.intermine.model.testmodel.Department;
import org.intermine.model.testmodel.Employee;

import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;

import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;

import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.DataSetLdr;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

public class FullTimeLdr implements DataSetLdr
{
    private static final Factory FAC = new Factory() {
        public Object create() {
            return new int[2];
        }
    };

    private DefaultCategoryDataset dataset;
    private final ObjectStore os;
    private Results results;
    private int total = 0;
    private final Map<String, int[]> resultTable = 
        LazyMap.decorate(new TreeMap<String, int[]>(), FAC);

    public FullTimeLdr(InterMineBag bag, ObjectStore os, String extra) {
        super();
        this.os = os;
        
        dataset = new DefaultCategoryDataset();

        Query q = getQuery(bag);

        results = os.execute(q);
        Iterator<?> iter = results.iterator();

        Set<String> depNames = new HashSet<String>();

        while (iter.hasNext()) {
            ResultsRow<?> resRow = (ResultsRow<?>) iter.next();

            Department dep = (Department) resRow.get(0);
            Employee emp = (Employee) resRow.get(1);
            depNames.add(dep.getName());

            int[] vals = resultTable.get(dep.getName());
            vals[emp.getFullTime() ? 1 : 0]++;
            resultTable.put(dep.getName(), vals);
        }

        total = depNames.size();

        for (String depName: resultTable.keySet()) {
            int[] vals = resultTable.get(depName);
            dataset.addValue(0 - vals[0], "Part-Time", depName);
            dataset.addValue(0 + vals[1], "Full-Time", depName);
        }
        
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Query getQuery(InterMineBag bag) {
        PathQuery pq = new PathQuery(os.getModel());

        if ("CEO".equals(bag.getType())) {
            pq.addViews("CEO.company.departments.name", "CEO.company.departments.employees.fullTime");
            pq.addConstraint(Constraints.in("CEO", bag.getName()));
        } else if ("Company".equals(bag.getType())) {
            pq.addViews("Department.name", "Department.employees.fullTime");
            pq.addConstraint(Constraints.in("Department.company", bag.getName()));
        } else {
            throw new IllegalArgumentException("Bag of unsuitable type: " + bag.getType());
        }

        Map<String, InterMineBag> bags 
            = new HashMap<String, InterMineBag>();
        bags.put(bag.getName(), bag);
        try {
            return MainHelper.makeQuery(pq, bags, new HashMap(), 
                    null, new HashMap());
        } catch (ObjectStoreException e) {
            throw new RuntimeException("Error running query", e);
        }
    }
    
    @Override
    public Dataset getDataSet() {
        return dataset;
    }

    @Override
    public Results getResults() {
        return results;
    }

    @Override
    public int getWidgetTotal() {
        return total;
    }
}
