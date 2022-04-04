package com.example.demo.user;

import com.example.demo.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/jpa")
public class UserJpaController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/users")
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public EntityModel<User> retrieveUser(@PathVariable int id) {
        Optional<User> user = userRepository.findById(id);
        if  (!user.isPresent()) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }

        EntityModel<User> userEntityModel = new EntityModel<>(user.get());
        WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).retrieveAllUsers());
        userEntityModel.add(linkTo.withRel("all-users"));

        return userEntityModel;
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User save = userRepository.save(user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(save.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/users/{id}/posts")
    public List<Post> retrieveAllPostsByUser(@PathVariable int id) {
        Optional<User> user = userRepository.findById(id);
        if  (!user.isPresent()) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }

//        EntityModel<User> userEntityModel = new EntityModel<>(user.get());
//        WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).retrieveAllUsers());
//        userEntityModel.add(linkTo.withRel("all-users"));

        return user.get().getPosts();
    }

    @GetMapping({"/users/{id}/posts/{postId}"})
    public EntityModel<Post> retrievePostsById(@PathVariable int id, @PathVariable int postId) {
        Optional<User> user = userRepository.findById(id);
        if  (!user.isPresent()) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }
        Optional<Post> post = postRepository.findById(postId);
        if  (!post.isPresent()) {
            throw new UserNotFoundException(String.format("post ID[%s] not found", postId));
        }

        EntityModel<Post> postEntityModel = new EntityModel<>(post.get());
        WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).retrieveAllUsers());
        postEntityModel.add(linkTo.withRel("all-users"));

        return postEntityModel;
    }

    @PostMapping("/users/{id}/posts")
    public ResponseEntity<Post> createPost(@PathVariable int id, @Valid @RequestBody Post post) {
        Optional<User> user = userRepository.findById(id);
        if  (!user.isPresent()) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }
        post.setUser(user.get());
        Post savedPost = postRepository.save(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedPost.getId())
                .toUri();

//         return ResponseEntity.created(location).body(savedUser);
        return ResponseEntity.created(location).build();
    }
}
