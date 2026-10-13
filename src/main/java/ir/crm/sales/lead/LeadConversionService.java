package ir.crm.sales.lead;

import ir.crm.sales.contact.Contact;
import ir.crm.sales.contact.ContactRepository;
import ir.crm.sales.lead.dto.LeadConversionResponse;
import ir.crm.sales.lead.dto.LeadConvertRequest;
import ir.crm.sales.opportunity.Opportunity;
import ir.crm.sales.opportunity.OpportunityRepository;
import ir.crm.sales.opportunity.OpportunitySource;
import ir.crm.sales.opportunity.OpportunityStage;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationIndustry;
import ir.crm.sales.organization.OrganizationRepository;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.LookupResponse;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vtiger-style "Convert lead": creates an organization and a contact (and optionally an opportunity) atomically. */
@Service
@RequiredArgsConstructor
@Transactional
public class LeadConversionService {

    private final LeadRepository leads;
    private final OrganizationRepository organizations;
    private final ContactRepository contacts;
    private final OpportunityRepository opportunities;
    private final ApplicationEventPublisher eventPublisher;

    public LeadConversionResponse convert(Long leadId, LeadConvertRequest request) {
        Lead lead = leads.findById(leadId).orElseThrow(() -> new ResourceNotFoundException("lead", leadId));
        if (lead.getStatus() == LeadStatus.CONVERTED) throw new BusinessRuleException("rule.lead.alreadyConverted");

        String orgName = hasText(lead.getCompany()) ? lead.getCompany().trim() : lead.getName();
        if (!hasText(orgName)) throw new BusinessRuleException("rule.lead.convertMissingName");

        Organization org = Organization.builder()
                .name(orgName)
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .city(lead.getCity())
                .assignedTo(lead.getAssignedTo())
                .industry(lead.getIndustry() != null ? OrganizationIndustry.fromKey(lead.getIndustry().key()) : null)
                .build();
        org = organizations.saveAndFlush(org);
        publishEvent("organizations", org.getId(), ChangeType.CREATED, org.getName());

        Contact contact = Contact.builder()
                .name(lead.getName())
                .organization(org)
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .city(lead.getCity())
                .assignedTo(lead.getAssignedTo())
                .build();
        contact = contacts.saveAndFlush(contact);
        publishEvent("contacts", contact.getId(), ChangeType.CREATED, contact.getName());

        LookupResponse opportunityRef = null;
        if (request.createOpportunity()) {
            Opportunity opp = Opportunity.builder()
                    .name(hasText(request.opportunityName()) ? request.opportunityName().trim() : orgName)
                    .organization(org)
                    .contact(contact)
                    .stage(OpportunityStage.QUALIFICATION)
                    .amount(request.opportunityAmount() != null ? request.opportunityAmount()
                            : lead.getExpectedRevenue() != null ? lead.getExpectedRevenue() : BigDecimal.ZERO)
                    .probability(20)
                    .source(lead.getSource() != null ? OpportunitySource.fromKey(lead.getSource().key()) : null)
                    .assignedTo(lead.getAssignedTo())
                    .build();
            opp = opportunities.saveAndFlush(opp);
            publishEvent("opportunities", opp.getId(), ChangeType.CREATED, opp.getName());
            opportunityRef = new LookupResponse(String.valueOf(opp.getId()), opp.getName());
        }

        lead.markConverted();
        leads.saveAndFlush(lead);
        publishEvent("leads", lead.getId(), ChangeType.UPDATED, lead.getName());

        return new LeadConversionResponse(
                new LookupResponse(String.valueOf(org.getId()), org.getName()),
                new LookupResponse(String.valueOf(contact.getId()), contact.getName()),
                opportunityRef
        );
    }

    private void publishEvent(String entityType, Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                entityType, id, changeType, text, null, Instant.now()
        ));
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
