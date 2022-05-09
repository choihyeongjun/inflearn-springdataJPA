package study.datajpa.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {
   // List<Member> findByUsernameAndAgeGreaterThen();

   // @Query(name="Member.findByUsername") //얘 지워도 돌아감감 1.메소드명과 같은놈 먼저 찾음 2. 없으면 JPA리포지토리 안에들어있는거 호출
   List<Member>findByUsername(@Param("username")String username);//네임드 쿼리 실무에서 잘 안씀,장점: 로딩시점에서 오류 찾음

    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id,m.username,t.name) from Member m join m.team t")
    List<MemberDto>findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String>names);

    List<Member> findListByUsername(String name);//컬렉션
    Member findMemberByUsername(String username);//단건
    Optional<Member> findOptionalByUsername(String username);//단건 optional

    @Query(value="select m from Member m left join m.team t"
            ,countQuery="select count(m.username) from Member m") //쿼리와 카운트 쿼리 분리 가능 이걸통해 토탈 카운트 성능 향상 가능 이유 카운트는 조인할필요없어서
    Page<Member> findByAge(int age, Pageable pageable);//datajpaapge 반환타입 page로

    @Modifying(clearAutomatically = true)//벌크성 쿼리 데이터 일치를 위해 사용
    @Query("update Member m set m.age = m.age+1 where m.age >= :age")
    int bulkAgePlus(@Param("age")int age);//얘는 써줄때 modifying이있어야 수정가능하다

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})//spring data가져올때 jpql작성안하고 기존에 만들어잔거 가져올때 사용 이러면 팀꺼까지 같이 가져옴
    List<Member>findAll();

    @EntityGraph(attributePaths = {"team"})//내부적으로 fetchjoin하는거
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);
    @QueryHints(value=@QueryHint(name="org.hibernate.readOnly",value="true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    List<UsernameOnly>findProjectionsByUsername(@Param("username") String username);
}
