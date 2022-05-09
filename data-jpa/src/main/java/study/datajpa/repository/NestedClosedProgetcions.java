package study.datajpa.repository;

public interface NestedClosedProgetcions {
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo{
        String getName();
    }
}
