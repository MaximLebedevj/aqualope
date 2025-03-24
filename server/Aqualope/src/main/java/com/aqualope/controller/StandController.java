package com.aqualope.controller;

import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;


@RestController
@RequestMapping("/stand")
public class StandController {

    private final AtomicReference<Process> runningProcess = new AtomicReference<>();

    @PostMapping("/on")
    public String runPythonScript() {
        if (runningProcess.get() != null) {
            return "Script is already running!";
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "main.py");
            processBuilder.directory(new File("/root/stand"));
            Process process = processBuilder.start();
            runningProcess.set(process);

            return "Stand started successfully!";
        } catch (IOException e) {
            return "Error starting stand: " + e.getMessage();
        }
    }

    @PostMapping("/off")
    public String stopPythonScript() {
        Process process = runningProcess.get();
        if (process != null) {
            process.destroy();
            runningProcess.set(null);
            return "Stand stopped successfully!";
        } else {
            return "No stand currently running.";
        }
    }
}
