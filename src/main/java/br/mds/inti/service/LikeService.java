package br.mds.inti.service;

import br.mds.inti.model.entity.Like;
import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.LikePk;
import br.mds.inti.repositories.LikeRepository;
import br.mds.inti.repositories.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class LikeService {

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    PostRepository postRepository;

    @Transactional
    public void likePost(Profile profile, UUID postId) {

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        Like like = likeRepository.findByProfileAndPostId(profile, postId).orElse(null);
        if (like != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Post already liked by this user");

        like = new Like();
        LikePk likePk = new LikePk(profile.getId(), postId);

        like.setId(likePk);
        like.setPost(post);
        like.setProfile(profile);
        like.setCreatedAt(Instant.now());

        likeRepository.save(like);
        postRepository.updateLikesCount(post.getLikesCount() + 1, post.getId());

    }

    @Transactional
    public void unlikePost(Profile profile, UUID postId) {

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        Like like = likeRepository.findByProfileAndPostId(profile, postId).orElse(null);
        if (like == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not liked by this user");

        likeRepository.delete(like);

        postRepository.updateLikesCount(post.getLikesCount() - 1, postId);

    }
}
