package com.neosuniversity.video.repository.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.neosuniversity.video.entities.Country;
import com.neosuniversity.video.repository.CountryRepository;
import com.neosuniversity.video.repository.util.CountryRepositoryUtil;

import lombok.extern.slf4j.Slf4j;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestCountryRepository implements CountryRepositoryUtil {
	
	@Autowired
	private CountryRepository countryRepository;
	
	private List<Country> countriesList;
	private Map<String,Country> countriesMap;
	
	@BeforeAll
	public void beforeAllInit() {
		 log.info("::::::::::::::::Running - populate countries ::::::::::::::::");
		countriesList = CountryRepositoryUtil.populateCountries();
		countriesMap = CountryRepositoryUtil.populateCountriesMap();
	}

	@AfterAll
    public  void cleanUp(){
		log.info("::::::::::::::::CleanUp() countries ::::::::::::::::");
		
        countriesList.stream().forEach(country ->{
        	log.debug("REMOVE COUNTRY: " +country.getDescription());
			countryRepository.delete(country);
		});
		
		assertThat(countryRepository.findById(1L)).isNotPresent();
	}
	
	@Test
	@Order(1)    
	public void testCreateCountry() {
		log.info("-----> Executing CREATE Country");
		countryRepository.save(countriesMap.get(MEXICO));
		
		Optional<Country> countryDB = countryRepository.findById(KEY_COUNTRY_1);
		assertThat(countryDB).isNotEmpty();
	}
	
	@Test
	@DisplayName("😱")
	@Order(2)    
	public void testReadCountry() {
		log.info("-----> Executing READ Country");
		List<Country> lstCountries = countryRepository.findByDescriptionContaining(MEXICO);
		
		assertThat(lstCountries).isNotEmpty()
								.hasSize(SIZE_1)
								.extracting(Country::getDescription)
								.containsExactly(MEXICO)
								.doesNotContainNull();
	}
	@Test
	@Order(3)    
	public void testReadPageableCountry() {
		Map<String,Country> countriesRead = countriesMap;
		countriesRead.entrySet().removeIf(entry -> entry.getValue().getDescription().equals(MEXICO));
		
		countriesRead.entrySet().stream().forEach(action->{
			countryRepository.save(action.getValue());
		});
		
		Pageable pageable = PageRequest.of(SIZE_0, SIZE_2,Sort.by(DESCRIPTION_FIELD).ascending());
		Page<Country> pageCountry = null;
		int countpage = -1;
		do {
			countpage++;
			pageCountry = countryRepository.findAll(pageable);
			int number = pageCountry.getNumber();
			int numberOfElements = pageCountry.getNumberOfElements();
			int size = pageCountry.getSize();
			long totalElements = pageCountry.getTotalElements();
			int totalPages = pageCountry.getTotalPages();
			log.debug(
					"page info - page number {}, numberOfElements: {}, size: {}, "
							+ "totalElements: {}, totalPages: {}",
					number, numberOfElements, size, totalElements, totalPages);
			
			if(countpage==SIZE_0){
				assertThat(pageCountry.getNumber()).isEqualTo(0);
				assertThat(pageCountry.getNumberOfElements()).isEqualTo(SIZE_2);
				assertThat(pageCountry.getSize()).isEqualTo(SIZE_2);
				assertThat(pageCountry.getTotalElements()).isEqualTo(SIZE_5);
				assertThat(pageCountry.getTotalPages()).isEqualTo(SIZE_3);
				assertThat(pageCountry).isNotEmpty()
									   .hasSize(SIZE_2)
									   .extracting(Country::getDescription)
									   .containsExactly(ARGENTINA,BRAZIL)
									   .doesNotContainNull();
			}
			if(countpage==SIZE_1){	
				assertThat(pageCountry.getNumber()).isEqualTo(SIZE_1);
				assertThat(pageCountry.getNumberOfElements()).isEqualTo(SIZE_2);
				assertThat(pageCountry.getSize()).isEqualTo(SIZE_2);
				assertThat(pageCountry.getTotalElements()).isEqualTo(SIZE_5);
				assertThat(pageCountry.getTotalPages()).isEqualTo(SIZE_3);
				assertThat(pageCountry).isNotEmpty()
									   .hasSize(SIZE_2)
									   .extracting(Country::getDescription)
									   .containsExactly(CANADA,COLOMBIA)
									   .doesNotContainNull();
			}
			if(countpage==SIZE_2){
				assertThat(pageCountry.getNumber()).isEqualTo(SIZE_2);
				assertThat(pageCountry.getNumberOfElements()).isEqualTo(SIZE_1);
				assertThat(pageCountry.getSize()).isEqualTo(SIZE_2);
				assertThat(pageCountry.getTotalElements()).isEqualTo(SIZE_5);
				assertThat(pageCountry.getTotalPages()).isEqualTo(SIZE_3);
				assertThat(pageCountry).isNotEmpty()
									   .hasSize(SIZE_1)
									   .extracting(Country::getDescription)
									   .containsExactly(MEXICO)
									   .doesNotContainNull();
			}
			pageable = pageCountry.nextPageable();
			
		} while (pageable.hasPrevious());
		log.debug("COUNT: "+countpage);
		
	}

	@Test
	@Order(4)    
	public void testUpdateCountry() {
		log.info("-----> Executing UPDATE Country");
		Optional<Country> countryDB = countryRepository.findById(KEY_COUNTRY_1);
		countryDB.get().setDescription(MEXICO_UPDATE);
		
		countryRepository.save(countryDB.get());
		
		Optional<Country> countryUpdate = countryRepository.findById(KEY_COUNTRY_1);
		log.debug("####: " +countryUpdate.get().toString());
		assertThat(countryUpdate).isNotEmpty()
								 .get()
								 .extracting(Country::getDescription)
								 .descriptionText()
								 .equalsIgnoreCase(MEXICO_UPDATE);
	}
	
	@Test
	@Order(5)    
	public void testDeleteCountry() {
		log.info("-----> Executing DELETE Country");
		Optional<Country> countryDB = countryRepository.findById(KEY_COUNTRY_1);
		countryRepository.delete(countryDB.get());
		
		assertThat(countryRepository.findById(1L)).isNotPresent();
	}
}
