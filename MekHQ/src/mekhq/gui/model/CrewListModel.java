/**
 *
 */
package mekhq.gui.model;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import megamek.common.Aero;
import megamek.common.Tank;
import megamek.common.VTOL;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.unit.Unit;
import mekhq.gui.BasicInfo;

/**
 * Model for a list that displays a unit's crew with their role.
 *
 * @author Neoancient
 */
public class CrewListModel extends AbstractListModel<Person> {
    private static final long serialVersionUID = 3584521762881297199L;

    enum CrewRole {
        COMMANDER (0, "Commander"),
        CONSOLE_CMDR (1, "Commander"),
        PILOT (2, "Pilot"),
        NAVIGATOR (3, "Navigator"),
        DRIVER (4, "Driver"),
        GUNNER (5, "Gunner"),
        TECH_OFFICER (6, "Tech Officer"),
        CREW (7, "Crew");

        private final int sortOrder;
        private final String displayName;

        public int getSortOrder() {
            return sortOrder;
        }

        public String getDisplayName() {
            return displayName;
        }

        CrewRole(int sortOrder, String displayName) {
            this.sortOrder = sortOrder;
            this.displayName = displayName;
        }

        public static CrewRole getCrewRole(Person p, Unit u) {
            if (u.usesSoloPilot()) {
                return PILOT;
            } else if (u.isCommander(p) && u.getEntity().getCrew().getSlotCount() == 1) {
                return COMMANDER;
            } else if (u.getEntity() instanceof Tank && u.isTechOfficer(p)) {
                return CONSOLE_CMDR;
            } else if (u.isDriver(p)) {
                if (u.getEntity() instanceof VTOL || u.getEntity() instanceof Aero) {
                    return PILOT;
                } else {
                    return DRIVER;
                }
            } else if (u.isNavigator(p)) {
                return NAVIGATOR;
            } else if (u.isGunner(p)) {
                return GUNNER;
            } else if (u.isTechOfficer(p)) {
                return TECH_OFFICER;
            } else {
                return CREW;
            }
        }

    }

    Unit unit;
    List<Person> crew;

    public void setData(final Unit u) {
        this.unit = u;
        this.crew = new ArrayList<>(u.getCrew());
        crew.sort(Comparator.comparingInt(p -> CrewRole.getCrewRole(p, u).getSortOrder()));
        fireContentsChanged(this, 0, crew.size());
    }

    @Override
    public int getSize() {
        return crew.size();
    }
    @Override
    public Person getElementAt(int index) {
        if (index < 0 || index >= crew.size()) {
            return null;
        }
        return crew.get(index);
    }

    public ListCellRenderer<Person> getRenderer() {
        return new CrewRenderer();
    }

    public class CrewRenderer extends BasicInfo implements ListCellRenderer<Person> {
        private static final long serialVersionUID = -1742201083598095886L;

        public CrewRenderer() {
            super();
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Person> list, Person value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Component c = this;
            setOpaque(true);
            Person p = (Person)getElementAt(index);
            StringBuilder sb = new StringBuilder("<html><font size='2'><b>")
                    .append(p.getFullTitle())
                    .append("</b><br/>")
                    .append(CrewRole.getCrewRole(p, unit).getDisplayName())
                    .append(" (");
            String gunSkill = SkillType.getGunnerySkillFor(unit.getEntity());
            String driveSkill = SkillType.getDrivingSkillFor(unit.getEntity());
            if (p.hasSkill(gunSkill)) {
                sb.append(p.getSkill(gunSkill).getFinalSkillValue());
            } else {
                sb.append("-");
            }
            sb.append("/");
            if (p.hasSkill(driveSkill)) {
                sb.append(p.getSkill(driveSkill).getFinalSkillValue());
            } else {
                sb.append("-");
            }
            sb.append(")</font></html>");
            setHtmlText(sb.toString());
            if (isSelected) {
                highlightBorder();
            } else {
                unhighlightBorder();
            }
            setPortrait(p);
            return c;
        }
    }

}
