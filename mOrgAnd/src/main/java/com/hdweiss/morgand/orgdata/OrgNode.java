package com.hdweiss.morgand.orgdata;

import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.hdweiss.morgand.utils.PreferenceUtils;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DatabaseTable(tableName = "OrgNodes")
public class OrgNode {

    public enum Type {
        File, Directory, Date, Headline, Body, Drawer, Checkbox, Setting
    }

    public enum State {
        Clean, Dirty, Deleted
    }

    @DatabaseField(generatedId = true)
    public int Id;

    public static final String PARENT_FIELD_NAME = "parent";
    @DatabaseField(foreign = true, columnName = PARENT_FIELD_NAME, foreignAutoRefresh=true, canBeNull=true, maxForeignAutoRefreshLevel=3)
    public OrgNode parent;

    @ForeignCollectionField(eager = false, foreignFieldName = PARENT_FIELD_NAME)
    public ForeignCollection<OrgNode> children;

    @DatabaseField
    public Type type;

    public static final String LEVEL_FIELD_NAME = "level";
    @DatabaseField(columnName = LEVEL_FIELD_NAME)
    public int level = 0;

    public static final String LINENUMBER_FIELD_NAME = "lineNumber";
    @DatabaseField(columnName =  LINENUMBER_FIELD_NAME)
    public int lineNumber = -1;

    public static final String STATE_FIELD_NAME = "state";
    @DatabaseField(columnName = STATE_FIELD_NAME)
    public State state = State.Clean;

    public static final String FILE_FIELD_NAME = "file";
    @DatabaseField(foreign =  true, columnName = FILE_FIELD_NAME)
    public OrgFile file;


    public static final String TITLE_FIELD_NAME = "title";
    @DatabaseField(columnName = TITLE_FIELD_NAME)
    public String title;

    @DatabaseField
    public String tags;

    @DatabaseField
    public String inheritedTags;


    public String toString() {
        return title + "\t" + tags;
    }

    public String toStringRecursively() {
        StringBuilder builder = new StringBuilder(toString());

        for(OrgNode child : children)
            builder.append("\n").append(child.toString());

        return builder.toString();
    }

    public boolean isEditable() {
        if (type == Type.File || type == Type.Directory)
            return false;

        if (file.isEditable() == false)
            return false;

        return true;
    }

    public String getProperty(String propertyName) {
        if (type == Type.Headline) {
            Pattern propertyPattern = Pattern.compile(":" + propertyName + ":\\s+(.+)");
            for(OrgNode child: children) {
                if (child.type == Type.Drawer) {
                    Matcher matcher = propertyPattern.matcher(child.title);
                    if (matcher.find()) {
                        return matcher.group(1).trim();
                    }
                }
            }

            return null;
        }

        if (parent != null)
            return parent.getProperty(propertyName);
        return null;
    }

    public String getTitle() {
        if (type != Type.Headline) {
            if (parent != null) {
                return parent.getTitle();
            }
            else {
                return "";
            }
        }

        return title.replaceAll("^\\** ", "");
    }

    public String getTodo() {
        if (type == Type.Headline) {

            Matcher matcher = OrgNodeUtils.headingPattern.matcher(title);
            if (matcher.find())
                return matcher.group(1);
        }
        return "";
    }

    public String getBody() {
        if (type == Type.Headline) {
            StringBuilder body = new StringBuilder();
            for(OrgNode child: children) {
                if (child.type == Type.Body)
                    body.append(child.title);
            }
            return body.toString();
        } else if (parent != null)
            return  parent.getBody();

        return title;
    }

    public ArrayList<OrgNode> getDisplayChildren() {
        ArrayList<OrgNode> nodes = new ArrayList<OrgNode>();
        boolean showSettings = PreferenceUtils.showSettings();
        boolean showDrawers = PreferenceUtils.showDrawers();
        for(OrgNode node: children) {
            switch (node.type) {
                case Drawer:
                    if (showDrawers)
                        nodes.add(node);
                    break;

                case Setting:
                    if (showSettings)
                        nodes.add(node);
                    break;

                default:
                    nodes.add(node);
            }
        }
        return nodes;
    }

    public OrgNode addChild(Type type, String title) {
        OrgNode node = new OrgNode();
        node.parent = this;
        node.file = this.file;
        node.type = type;
        node.title = title;

        OrgNodeRepository.getDao().create(node);
        return node;
    }

    /**
     * @return line number of next node on same or higher level than current node.
     */
    public int getNextNodeLineNumber() {
        try {
            Where<OrgNode, Integer> where = OrgNodeRepository.getDao().queryBuilder().orderBy(LINENUMBER_FIELD_NAME, true).where();
            where.eq(FILE_FIELD_NAME, file).and().gt(LINENUMBER_FIELD_NAME, lineNumber).and().le(LEVEL_FIELD_NAME, level);
            OrgNode node = where.queryForFirst();
            if (node != null)
                return node.lineNumber;
            else
                return Integer.MAX_VALUE; // Current node seems to be last in file
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<OrgNodeDate> getOrgNodeDates() {
        ArrayList<OrgNodeDate> dates = new ArrayList<OrgNodeDate>();

        Matcher matcher = OrgNodeUtils.dateMatcher.matcher(title);
        while(matcher.find()) {
            String type = matcher.group(1);
            String startDate = matcher.group(2);
            String endDate = matcher.group(3);

            try {
                OrgNodeDate orgNodeDate = new OrgNodeDate(startDate);
                orgNodeDate.type = type != null ? type : "";

                orgNodeDate.setTitle(getTitle());
                dates.add(orgNodeDate);
            } catch (IllegalArgumentException ex) {}
        }

        return dates;
    }


    public static class OrgNodeCompare implements Comparator<OrgNode> {
        @Override
        public int compare(OrgNode node1, OrgNode node2) {
            return node1.title.compareTo(node2.title);
        }
    }
}
