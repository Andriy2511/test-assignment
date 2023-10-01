package com.example.testassignment.controller;

import com.example.testassignment.configuration.UserConfiguration;
import com.example.testassignment.model.User;
import com.example.testassignment.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class TestUserController {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserConfiguration userConfiguration;

    @Before
    public void initParameters(){
        when(userConfiguration.getMinAge()).thenReturn(18);
    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        mvc.perform(post("/users/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@gmail.com\",\"firstName\":\"Robert\", " +
                                "\"lastName\":\"Martin\",\"birthDate\":\"1999-01-02\"}")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));
    }

    @Test
    public void testCreateUserYoungerThanEighteen() throws Exception {
        mvc.perform(post("/users/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@gmail.com\",\"firstName\":\"Robert\"," +
                                "\"lastName\":\"Martin\",\"birthDate\":\"2010-01-02\"}")
                )
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("User must be older than 18"));
    }

    @Test
    public void TestCreateUserWithoutRequiredFields() throws Exception {
        mvc.perform(post("/users/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@gmail.com\",\"firstName\":\"Robert\",\"lastName\":\"Martin\", " +
                                "\"address\":\"Ukraine\", \"phoneNumber\": \"0994253120\"}")
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testUpdateFieldsSuccessfully() throws Exception {
        User user = new User(1L, "test@gmail.com", "Robert", "Martin",
                LocalDate.of(1998, 10, 10), "Lviv", "0994546630");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        mvc.perform(patch("/users/updateUserFields/{userId}", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"update@ukr.net\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("The user has been updated"));
    }

    @Test
    public void testUpdateUserWhichDoesntExist() throws Exception{
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        mvc.perform(patch("/users/updateUserFields/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"update@ukr.net\"}"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("The user cannot be null"));
    }

    @Test
    public void testUpdateAllFieldsSuccessfully() throws Exception {
        User user = new User(1L, "test@gmail.com", "Robert", "Martin",
                LocalDate.of(1998, 10, 10), "Lviv", "0994546630");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        mvc.perform(put("/users/updateAllUserFields/{userId}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@ukr.net\",\"firstName\":\"John\",\"lastName\":\"Smite\"" +
                                ", \"birthDate\":\"1992-01-02\", \"address\":\"Ukraine\", \"phoneNumber\": \"0994253120\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("The user has been updated"));
    }

    @Test
    public void testUpdateAllUserFieldsWithNotValidEmail() throws Exception{
        User user = new User(1L, "test@gmail.com", "Robert", "Martin",
                LocalDate.of(1998, 10, 10), "Lviv", "0994546630");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        mvc.perform(put("/updateAllUserFields/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalidEmail\",\"firstName\":\"Robert\", " +
                                "\"lastName\":\"Martin\",\"birthDate\":\"1999-01-02\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testDeleteUserSuccessfully() throws Exception {
        User user = new User(1L, "test@gmail.com", "Robert", "Martin",
                LocalDate.of(1998, 10, 10), "Lviv", "0994546630");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        mvc.perform(delete("/users/deleteUserById/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("The user has been deleted"));
    }

    @Test
    public void testDeleteUserWhichDoesntExist() throws Exception{
        when(userRepository.findAllByEmail(any())).thenReturn(null);

        mvc.perform(delete("/users/deleteUserByEmail/{email}", "mail@gmail.com"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("User not found"));
    }

    @Test
    public void testSearchByDateWhereFromIsBiggerThanTo() throws Exception{
        mvc.perform(get("/users/searchUsersByBirthDayRange")
                        .param("from", "1990-01-01")
                        .param("to", "1940-12-31"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("The parameter 'from' must be less than the parameter 'to'"));
    }
}
