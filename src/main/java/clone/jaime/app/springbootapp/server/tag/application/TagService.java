package clone.jaime.app.springbootapp.server.tag.application;

import clone.jaime.app.springbootapp.server.tag.domain.entity.Tag;
import clone.jaime.app.springbootapp.server.tag.infra.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;


    public Tag findOrCreateNew(String tagTitle){
        return tagRepository.findByTitle(tagTitle)
                .orElseGet(
                        () -> tagRepository.save(
                                Tag.builder()
                                        .title(tagTitle)
                                        .build()));
    }

}
