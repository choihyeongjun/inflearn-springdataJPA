package study.datajpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;
import study.datajpa.repository.MemberRepository;
import study.datajpa.repository.TeamRepository;
import study.datajpa.repository.UsernameOnly;

import javax.persistence.EntityManager;
import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;
    @Test
    public void testMember(){
        Member member=new Member("UserA");
        Member savedMember=memberRepository.save(member);

        Member findMember=memberRepository.findById(savedMember.getId()).get();
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    }

    @Test
    public void testQuery(){
        Member m1=new Member("AAA",10);
        Member m2=new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result=memberRepository.findUser("AAA",10);
        assertThat(result.get(0)).isEqualTo(m1);

    }
    @Test
    public void findMemberDto(){
        Team team=new Team("teamA");
        teamRepository.save(team);
        Member m1=new Member("AAA",10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> result=memberRepository.findMemberDto();
        for(MemberDto s : result){
            System.out.println("s=" +s);
        }
    }
    @Test
    public void findUsernameList(){
        Member m1=new Member("AAA",10);
        Member m2=new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<String> result=memberRepository.findUsernameList();
        for(String s : result){
            System.out.println("s=" +s);
        }
    }

    @Test
    public void findByNames(){
        Member m1=new Member("AAA",10);
        Member m2=new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result=memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for(Member s : result){
            System.out.println("s=" +s);
        }
    }
    @Test
    public void returnType(){
        Member m1=new Member("AAA",10);
        Member m2=new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member>aaa=memberRepository.findListByUsername("AAA");
        Member aaa1=memberRepository.findMemberByUsername("AAA");
        Optional<Member> aaa2=memberRepository.findOptionalByUsername("AAA");
    }
    @Test
    public void paging() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        int age=10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,
                "username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest);
        Page<MemberDto>toMap=page.map(member->new MemberDto(member.getId(),member.getUsername(),"teamA"));

        List<Member>content=page.getContent();
        long totalElements=page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdate(){
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 21));
        memberRepository.save(new Member("member4", 40));
        memberRepository.save(new Member("member5", 19));

        int resultCount=memberRepository.bulkAgePlus(20);
       // em.flush();
    //    em.clear();//벌크성 쿼리는 영속성 데이터를 무시하고 바로 업데이트 떄려버림 그래서 지금 데이터를 조회하면 업데이트 전꺼를 가져옴
        //그렇기 떄문에 이러한 오류를 줄이기 위해 벌크성 쿼리는 작성후 플러시와 클리어로 다 날려줘야 일치함
        assertThat(resultCount).isEqualTo(3);
    }
    @Test
    public void findMemberLazy(){
        //member1->teamA
        //member2->teamB

        Team teamA=new Team("teamA");
        Team teamB=new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1=new Member("member1",10,teamA);
        Member member2=new Member("member2",20,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        em.flush();
        em.clear();

        //when
        List<Member>members=memberRepository.findAll();
        for(Member member :members){
            System.out.println(member.getUsername());
            System.out.println(member.getTeam().getName());
        }
    }
    @Test
    public void queryHint(){
        Member member1=new Member("member1",10);
        memberRepository.save(member1);
        em.flush();
        em.clear();
        Member findMember=memberRepository.findReadOnlyByUsername("member1");

        em.flush();
    }
    @Test
    public void lock(){
        Member member1=new Member("member1",10);
        memberRepository.save(member1);
        em.flush();
        em.clear();
        List<Member> findMember=memberRepository.findLockByUsername("member1");

        em.flush();
    }
    @Test
    public void callCustom(){
        List<Member>result=memberRepository.findMemberCustom();
    }

    @Test
    public void projections(){
        Team teamA=new Team("teanA");
        em.persist(teamA);
        Member m1=new Member("m1",10,teamA);
        Member m2=new Member("m2",20,teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();
        List<UsernameOnly>result=memberRepository.findProjectionsByUsername("m1");
        for(UsernameOnly usernameOnly: result){
            System.out.println("username"+usernameOnly);
        }
    }
}
