package io.littlegashk.webapp.service;

import io.littlegashk.webapp.rentity.Tag;
import io.littlegashk.webapp.rentity.Topic;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MigrationServiceImpl implements MigrationService {

  @Autowired
  io.littlegashk.webapp.rentity.TopicRepository rTopicRepo;

  @Autowired
  io.littlegashk.webapp.rentity.TagRepository rTagRepo;

  @Override
  public void migrateTags() {
    List<Topic> topics = rTopicRepo.findAll();
    topics.forEach(t->{
      String[] tags = t.getTags();
      for(String tag:tags){
        Tag tagEntity;
        if(rTagRepo.existsById(tag)){
          tagEntity = rTagRepo.getOne(tag);
        }else{
          tagEntity = new Tag().setTag(tag);
        }
        tagEntity.getTopics().add(t);
        rTagRepo.save(tagEntity);
      }
    });
  }
}
