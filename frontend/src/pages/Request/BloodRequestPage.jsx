import {
  Box,
  Button,
  Container,
  FormControl,
  FormLabel,
  Heading,
  Input,
  Stack,
  Text,
  useToast,
  VStack,
  Card,
  CardBody,
  Icon,
  Select,
  Textarea,
  NumberInput,
  NumberInputField,
  NumberInputStepper,
  NumberIncrementStepper,
  NumberDecrementStepper,
  Alert,
  AlertIcon,
} from '@chakra-ui/react';
import { FiHeart } from 'react-icons/fi';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

function BloodRequestPage() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    bloodGroup: '',
    unitsRequired: 1,
    hospital: '',
    location: user?.location || '',
    urgency: 'MEDIUM',
    requiredDate: '',
    description: '',
  });

  const [isLoading, setIsLoading] = useState(false);

  const bloodGroups = [
    'A+',
    'A-',
    'B+',
    'B-',
    'AB+',
    'AB-',
    'O+',
    'O-',
  ];

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleNumberChange = (valueString) => {
    setFormData((prev) => ({
      ...prev,
      unitsRequired: parseInt(valueString, 10) || 1,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setIsLoading(true);

    try {
      if (!formData.bloodGroup) {
        throw new Error('Please select a blood group');
      }

      if (!formData.location.trim()) {
        throw new Error('Please enter your location');
      }

      if (!formData.requiredDate) {
        throw new Error('Please select the required date');
      }

      /*
       * Backend endpoint:
       * POST /api/blood-requests
       *
       * Backend BloodRequestCreateDto expects:
       * bloodGroup
       * unitsRequired
       * hospital
       * location
       * urgency
       * requiredDate
       * description
       */

      const response = await fetch('/api/blood-requests', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({
          bloodGroup: formData.bloodGroup,
          unitsRequired: formData.unitsRequired,
          hospital: formData.hospital,
          location: formData.location,
          urgency: formData.urgency,
          requiredDate: formData.requiredDate,
          description: formData.description,
        }),
      });

      if (response.ok) {
        const newRequest = await response.json();

        toast({
          title: 'Request Submitted',
          description:
            'Your blood request has been submitted successfully!',
          status: 'success',
          duration: 5000,
          isClosable: true,
        });

        navigate('/recipient-dashboard', {
          state: {
            newRequestId: newRequest.id,
          },
        });

        return;
      }

      let errorMessage = 'Failed to submit blood request';

      try {
        const data = await response.json();

        errorMessage =
          data.message ||
          data.error ||
          errorMessage;

        if (data.fieldErrors) {
          const fieldMessages = Object.values(data.fieldErrors);

          if (fieldMessages.length > 0) {
            errorMessage = fieldMessages.join(', ');
          }
        }
      } catch {
        errorMessage =
          `Request failed with HTTP ${response.status}`;
      }

      throw new Error(errorMessage);
    } catch (error) {
      console.error('Blood request submission failed:', error);

      toast({
        title: 'Submission Failed',
        description:
          error.message ||
          'Something went wrong. Please try again later.',
        status: 'error',
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container maxW="2xl" py={12}>
      <VStack spacing={8}>
        <VStack spacing={4} textAlign="center">
          <Icon
            as={FiHeart}
            w={12}
            h={12}
            color="primary.500"
          />

          <Heading size="lg">
            Request Blood
          </Heading>

          <Text color="gray.600">
            Submit your blood request and we'll notify nearby donors
          </Text>
        </VStack>

        <Alert status="info">
          <AlertIcon />

          <Box>
            <Text fontWeight="medium">
              Important:
            </Text>

            <Text fontSize="sm">
              Please ensure all information is accurate.
              Emergency requests will be prioritized.
            </Text>
          </Box>
        </Alert>

        <Card w="full">
          <CardBody p={8}>
            <form onSubmit={handleSubmit}>
              <Stack spacing={6}>

                {/* Blood Group */}
                <FormControl isRequired>
                  <FormLabel>
                    Blood Group Needed
                  </FormLabel>

                  <Select
                    name="bloodGroup"
                    value={formData.bloodGroup}
                    onChange={handleChange}
                    placeholder="Select blood group"
                    isDisabled={isLoading}
                  >
                    {bloodGroups.map((group) => (
                      <option
                        key={group}
                        value={group}
                      >
                        {group}
                      </option>
                    ))}
                  </Select>
                </FormControl>

                {/* Units */}
                <FormControl isRequired>
                  <FormLabel>
                    Units Required
                  </FormLabel>

                  <NumberInput
                    value={formData.unitsRequired}
                    onChange={handleNumberChange}
                    min={1}
                    max={10}
                    isDisabled={isLoading}
                  >
                    <NumberInputField />

                    <NumberInputStepper>
                      <NumberIncrementStepper />
                      <NumberDecrementStepper />
                    </NumberInputStepper>
                  </NumberInput>
                </FormControl>

                {/* Hospital */}
                <FormControl>
                  <FormLabel>
                    Hospital / Medical Center
                  </FormLabel>

                  <Input
                    name="hospital"
                    value={formData.hospital}
                    onChange={handleChange}
                    placeholder="Enter hospital name"
                    isDisabled={isLoading}
                  />
                </FormControl>

                {/* Location */}
                <FormControl isRequired>
                  <FormLabel>
                    Location
                  </FormLabel>

                  <Input
                    name="location"
                    value={formData.location}
                    onChange={handleChange}
                    placeholder="Enter location"
                    isDisabled={isLoading}
                  />
                </FormControl>

                {/* Urgency */}
                <FormControl isRequired>
                  <FormLabel>
                    Urgency Level
                  </FormLabel>

                  <Select
                    name="urgency"
                    value={formData.urgency}
                    onChange={handleChange}
                    isDisabled={isLoading}
                  >
                    <option value="LOW">
                      Low - Planned
                    </option>

                    <option value="MEDIUM">
                      Medium - Within 48 hours
                    </option>

                    <option value="HIGH">
                      High - Within 24 hours
                    </option>

                    <option value="CRITICAL">
                      Critical - Immediate
                    </option>
                  </Select>
                </FormControl>

                {/* Required Date */}
                <FormControl isRequired>
                  <FormLabel>
                    Required By Date
                  </FormLabel>

                  <Input
                    name="requiredDate"
                    type="date"
                    value={formData.requiredDate}
                    onChange={handleChange}
                    min={new Date().toISOString().split('T')[0]}
                    isDisabled={isLoading}
                  />
                </FormControl>

                {/* Description */}
                <FormControl>
                  <FormLabel>
                    Additional Notes
                  </FormLabel>

                  <Textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    placeholder="Any additional information for donors"
                    rows={4}
                    maxLength={1000}
                    isDisabled={isLoading}
                  />
                </FormControl>

                {/* Submit */}
                <Button
                  type="submit"
                  colorScheme="primary"
                  size="lg"
                  fontSize="md"
                  isLoading={isLoading}
                  loadingText="Submitting request..."
                >
                  Submit Blood Request
                </Button>

              </Stack>
            </form>
          </CardBody>
        </Card>

        <Text
          textAlign="center"
          fontSize="sm"
          color="gray.500"
        >
          Your request will be shared with verified donors
          in your area. You'll receive notifications as soon
          as donors respond.
        </Text>
      </VStack>
    </Container>
  );
}

export default BloodRequestPage;