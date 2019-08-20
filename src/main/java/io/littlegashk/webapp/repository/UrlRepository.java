package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.UrlTopic;
import io.littlegashk.webapp.entity.UrlTopicId;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@EnableScan
public interface UrlRepository extends DynamoDBPagingAndSortingRepository<UrlTopic, UrlTopicId> {

    Page<UrlTopic> findAllByTag(String tag, Pageable pageable);

    default Set<String> findAllUrls(){
        boolean last = false;
        Set<String> urls  = new HashSet<>();
        while(!last) {
            Pageable pr = PageRequest.of(0, 1000);
            Page<UrlTopic> someTags = findAllByTag("URL", pr);
            last = someTags.isLast();
            urls.addAll(someTags.get().map(UrlTopic::getUrlString).collect(Collectors.toSet()));
        }
        return urls;
    }

    Page<UrlTopic> findUrlTopicByUrl(String tagKey,Pageable pageable);

    default Page<UrlTopic> findAllWithUrl(String url, int page) {
        PageRequest pr = PageRequest.of(page, 1000, Sort.Direction.DESC, "topicId");
        return findUrlTopicByUrl("URL|" + url, pr);
    }

}
