package com.ocean.probe;

import com.ocean.probe.controller.ProbeController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GridConfig {

    @Bean
    public ProbeController.Grid grid() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        grid.addObstacle(2, 2); // example obstacle
        return grid;
    }
}

