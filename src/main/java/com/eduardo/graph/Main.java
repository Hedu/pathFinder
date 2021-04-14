package com.eduardo.graph;

import com.eduardo.graph.model.BPMNDefinition;
import com.eduardo.graph.model.InputForm;
import com.eduardo.graph.model.LinkedName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final int UNIVERSAL_ERROR_CODE = -1;

    private static final String BPMN_FILE_URL = "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws" + ".com/prod" +
            "/engine-rest/process-definition/key/invoice/xml";


    public static void main(final String[] args) {

        try {
            final InputForm form = processInput(args);
            final BPMNDefinition definition = getBPMNDefinition();
            final BpmnModelInstance bpmnModelInstance =
                    Bpmn.readModelFromStream(new ByteArrayInputStream(definition.getXml().getBytes()));
            final List<String> path = getPath(bpmnModelInstance, form.getInitialNode(), form.getFinalNode());

            if (path.size() < 2) {
                System.out.println("Path not found");
                System.exit(UNIVERSAL_ERROR_CODE);
            }

            String successMessage = String.format("The path from %s to %s is: [%s]", form.getInitialNode(),
                    form.getFinalNode(), String.join(", ", path));
            System.out.println(successMessage);
        } catch (Exception e) {
            System.out.println("There was an error");
            e.printStackTrace(System.out);
            System.exit(UNIVERSAL_ERROR_CODE);
        }

    }


    /**
     * Validates the input and normalizes it
     * @param args
     * @return
     */
    private static InputForm processInput(final String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of parameters");
        }
        return new InputForm(args[0], args[1]);
    }

    /**
     * Gets the xml definition from the url and converts it to a POJO
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static BPMNDefinition getBPMNDefinition() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BPMN_FILE_URL)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper om = new ObjectMapper();
        return om.readValue(response.body(), BPMNDefinition.class);
    }

    /**
     * Gets the path (list of ids) from initialNode to finalNode (inclusive) or an empty list if the path isn't found.
     * The implementation is based on the A* algorithm
     * @param modelInstance
     * @param initialNode
     * @param finalNode
     * @return
     */
    private static List<String> getPath(final BpmnModelInstance modelInstance, final String initialNode,
                                        final String finalNode) {

        // The list of available flows (connections)
        Collection<SequenceFlow> flows = modelInstance.getModelElementsByType(SequenceFlow.class).stream().collect(Collectors.toUnmodifiableList());

        // The last node of our sequence (it points to null)
        LinkedName finalLinkedName = new LinkedName(finalNode, null);

        // We initialize the list of nodes to explore (nodes that point to the last node).
        // We are iterating backwards, from tail to head
        List<LinkedName> linkedNames =  flows.stream().filter(flow -> Objects.equals(flow.getTarget().getId(), finalNode))
                        .map(flow -> new LinkedName(flow.getSource().getId(), finalLinkedName))
                        .collect(Collectors.toCollection(LinkedList::new));

        // The list of elements that we don't want to explore. It's initialized with the final node.
        List<String> closedNames = new ArrayList<>();
        closedNames.add(finalNode);

        LinkedName resultingLinkedName = null;

        while (!linkedNames.isEmpty()) {
            LinkedName linkedName = linkedNames.get(0);
            if (linkedName.getName().equals(initialNode)) {
                resultingLinkedName = linkedName;
                break;
            }
            flows.stream().filter(flow -> Objects.equals(flow.getTarget().getId(), linkedName.getName()))
                    .map(flow -> new LinkedName(flow.getSource().getId(), linkedName))
                    .forEach(newLinkedName -> {
                        if (linkedNames.stream().noneMatch(ln -> ln.getName().equals(newLinkedName.getName())) &&
                            closedNames.stream().noneMatch(name -> name.equals(newLinkedName.getName()))) {
                                linkedNames.add(newLinkedName);
                        }
                    });
            linkedNames.remove(0);
            closedNames.add(linkedName.getName());
        }

        List<String> result = new ArrayList<>();
        while (resultingLinkedName != null) {
            result.add(resultingLinkedName.getName());
            resultingLinkedName = resultingLinkedName.getLinkedName();
        }

        return result;
    }

}
