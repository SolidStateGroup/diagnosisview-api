package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.api.controller.PublicController
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class PublicControllerTest extends Specification {
    def publicController = new PublicController()
    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicController).build()
    }

    def "get status"() throws Exception {
        when: "the status endpoint is called"

        def response = mockMvc
                .perform(MockMvcRequestBuilders.get("/public/status"))
                .andExpect(MockMvcResultMatchers.status().isOk())

        def content = response.andReturn().response.getContentAsString()

        then: "ok is returned"

        content.contains("ok")
    }
}
