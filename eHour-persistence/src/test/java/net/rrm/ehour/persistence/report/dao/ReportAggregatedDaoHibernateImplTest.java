/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.persistence.report.dao;

import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.*;
import net.rrm.ehour.persistence.dao.AbstractAnnotationDaoTest;
import net.rrm.ehour.report.reports.element.ActivityAggregateReportElement;
import net.rrm.ehour.util.DomainUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@SuppressWarnings({"deprecation"})
public class ReportAggregatedDaoHibernateImplTest extends AbstractAnnotationDaoTest {
    private static final Date OCT_1_2006 = new Date(2006 - 1900, 10 - 1, 1);
    private static final Date OCT_4_2006 = new Date(2006 - 1900, 10 - 1, 4);
    private static final DateRange OCT_1_TO_4 = new DateRange(OCT_1_2006, OCT_4_2006);
    private static final Date OCT_2_2006 = new Date(2006 - 1900, 10 - 1, 2);
    private static final Date OCT_2_2006_EOF = new Date(2007 - 1900, 2 - 1, 2, 23, 59, 59);

    @Autowired
    private ReportAggregatedDao reportAggregatedDAO;

    public ReportAggregatedDaoHibernateImplTest() {
        super("dataset-reportaggregated.xml");
    }

    @Test
    public void shouldGetMinMaxDateTimesheetEntry() {
        Date endDate = new Date(OCT_2_2006_EOF.getTime() + 999);
        DateRange range = reportAggregatedDAO.getMinMaxDateTimesheetEntry();

        assertEquals(OCT_2_2006, range.getDateStart());
        assertEquals(endDate, range.getDateEnd());
    }

    @Test
    public void shouldGetMinMaxDateTimesheetEntryForUser() {
        DateRange range = reportAggregatedDAO.getMinMaxDateTimesheetEntry(new User(1));
        Date endDate = new Date(OCT_2_2006_EOF.getTime() + 999);

        assertEquals(OCT_2_2006, range.getDateStart());
        assertEquals(endDate, range.getDateEnd());
    }

    @Test
    public void shouldGetCumulatedHoursPerActivityForUserAndDate() {
        DateRange dateRange = new DateRange(OCT_1_2006, new Date(2007 - 1900, 10, 30));

        List<User> users = Arrays.asList(UserObjectMother.createUser());

        List<ActivityAggregateReportElement> results = reportAggregatedDAO.getCumulatedHoursPerActivityForUsers(users, dateRange);

        // test if collection is properly initialized
        ActivityAggregateReportElement rep = results.get(0);
        assertEquals("eHour", rep.getActivity().getProject().getName());

        rep = results.get(0);
        //TODO-NK Need to figure out the turnover implementation in case of Activity
        assertEquals(0, rep.getTurnOver().floatValue(), 0.1);

        assertEquals(3, results.size());
    }

    @Test
    public void shouldGetCumulatedHoursPerActivityForUser() {
        List<User> users = Arrays.asList(UserObjectMother.createUser(), new User(2));

        List<ActivityAggregateReportElement> results = reportAggregatedDAO.getCumulatedHoursPerActivityForUsers(users);

        // test if collection is properly initialized
        ActivityAggregateReportElement rep = results.get(0);
        assertEquals("eHour", rep.getActivity().getProject().getName());

        assertEquals(38.7f, rep.getHours().floatValue(), 0.1);

        assertEquals(5, results.size());
    }

    @Test
    public void shouldGetCumulatedHoursPerActivityForUserProject() {
        List<User> users = Arrays.asList(UserObjectMother.createUser());
        List<Project> projects = Arrays.asList(ProjectObjectMother.createProject(1));

        List<ActivityAggregateReportElement> results = reportAggregatedDAO.getCumulatedHoursPerActivityForUsers(users, projects);

        // test if collection is properly initialized
        ActivityAggregateReportElement rep = results.get(0);
        assertEquals(38.7f, rep.getHours().floatValue(), 0.1);

        assertEquals(2, results.size());
    }

    @Test
    public void shouldGetCumulatedHoursPerActivityForUserProjectDate() {
        DateRange dateRange = OCT_1_TO_4;
        List<User> users = Arrays.asList(UserObjectMother.createUser());
        List<Project> projects = Arrays.asList(ProjectObjectMother.createProject(1));

        List<ActivityAggregateReportElement> results = reportAggregatedDAO.getCumulatedHoursPerActivityForUsers(users, projects, dateRange);

        ActivityAggregateReportElement rep = results.get(0);
        assertEquals(14f, rep.getHours().floatValue(), 0.1);

        assertEquals(2, results.size());
    }

    @Test
    public void shouldGetCumulatedHoursPerActivity() {
        List<ActivityAggregateReportElement> results = reportAggregatedDAO.getCumulatedHoursPerActivity(OCT_1_TO_4);

        assertEquals(3, results.size());
    }

    @Test
    public void shouldGetCumulatedHoursPerActivityForProjects() {
        List<Project> projects = Arrays.asList(ProjectObjectMother.createProject(1));

        List<ActivityAggregateReportElement> results = reportAggregatedDAO.getCumulatedHoursPerActivityForProjects(projects, OCT_1_TO_4);

        assertEquals(2, results.size());
    }

    @Test
    public void shouldGetActivitiesWithoutHours() {
        List<Activity> activities = reportAggregatedDAO.getActivitiesWithoutBookings(OCT_1_TO_4);

        List<Integer> activityIds = DomainUtil.getIdsFromDomainObjects(activities);
        Collections.sort(activityIds);

        assertArrayEquals(new Integer[]{7, 12}, activityIds.toArray());
    }
}
