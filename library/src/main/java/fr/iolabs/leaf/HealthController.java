package fr.iolabs.leaf;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	  public static double roundTo2Digits(double value) {
	    return Math.round(value * 100.0) / 100.0;
	  }

	  public static String readableMemory(long lmemory) {
	    double memory = (double) lmemory;
	    double kilo = 1024.0;

	    String memoryValue = "?";
	    String memoryUnit = "?";
	    if (memory >= (kilo * kilo * kilo)) {
	      memoryValue = "" + roundTo2Digits(memory / kilo / kilo / kilo);
	      memoryUnit = "Go";
	    } else if (memory >= (kilo * kilo)) {
	      memoryValue = "" + roundTo2Digits(memory / kilo / kilo);
	      memoryUnit = "Mo";
	    } else if (memory >= (kilo)) {
	      memoryValue = "" + roundTo2Digits(memory / kilo);
	      memoryUnit = "Ko";
	    } else {
	      memoryValue = "" + roundTo2Digits(memory);
	      memoryUnit = "o";
	    }
	    return memoryValue + memoryUnit;
	  }

    @CrossOrigin
    @PermitAll
    @GetMapping
    public ResponseEntity<Map<String, Object>> apiHealth() {
        Map<String, Object> response = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        response.put("maxMemory", readableMemory(maxMemory));
        response.put("totalMemory", readableMemory(totalMemory));
        response.put("freeMemory", readableMemory(freeMemory));
        response.put("usedMemory", readableMemory(totalMemory - freeMemory));

        return ResponseEntity.ok(response);
    }
}
