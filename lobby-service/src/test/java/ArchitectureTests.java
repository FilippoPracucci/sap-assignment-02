import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import common.hexagonal.Adapter;
import common.hexagonal.InBoundPort;
import common.hexagonal.OutBoundPort;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureTests {

	final ClassFileImporter importer = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests());

	final JavaClasses importedClasses =  importer.importPackages("lobby_service");
	final String domainPackage = "..domain..";
	final String applicationPackage = "..application..";
	final String infrastructurePackage = "..infrastructure..";

	@Test
    public void cleanArchitecture() {
    	
    	/* the domain should not depend on application/infrastructure */ 

    	var domainModelWithNoDeps = 
    			noClasses().that().resideInAPackage(domainPackage)
    			.should().dependOnClassesThat().resideInAPackage(applicationPackage)
    			.orShould().dependOnClassesThat().resideInAPackage(infrastructurePackage);
    	domainModelWithNoDeps.check(this.importedClasses);

    	/* it must have a layered architecture */
    	
    	var layeredRule = layeredArchitecture()
				.consideringAllDependencies()
				.layer("Domain").definedBy(domainPackage)
				.layer("Application").definedBy(applicationPackage)
				.layer("Infrastructure").definedBy(infrastructurePackage)
				.whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
				.whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
				.whereLayer("Domain").mayOnlyBeAccessedByLayers("Application","Infrastructure");
		layeredRule.check(this.importedClasses);
    }	
	
	@Test
    public void hexagonalArchitecture() {
    
		/* it must have a clean architecture */
		cleanArchitecture();
    	
    	/* all ports should be defined either in the application layer or in the domain layer */
    	
    	var portsRule = classes().that()
    				.areAnnotatedWith(InBoundPort.class).or()
    			  	.areAnnotatedWith(OutBoundPort.class)
    			  	.should().resideInAPackage(applicationPackage)
    			  	.orShould().resideInAPackage(domainPackage);
		portsRule.check(this.importedClasses);
  
    	/* all adapters should be defined in the infrastructure layer */
    	
    	var adaptersRule = classes().that()
    				.areAnnotatedWith(Adapter.class)
    			  	.should().resideInAPackage(infrastructurePackage);
		adaptersRule.check(this.importedClasses);
      	
    	
    }
}
