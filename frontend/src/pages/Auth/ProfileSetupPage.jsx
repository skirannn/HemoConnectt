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
  Switch,
  Textarea,
  Select,
  HStack,
  Progress,
  Badge,
} from '@chakra-ui/react';

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

function ProfileSetupPage() {
  const { user, updateUser } = useAuth();

  const [formData, setFormData] = useState({
    // Donor fields
    medicalConditions: '',
    medications: '',
    lastDonationDate: '',
    isAvailable: true,

    // Recipient fields
    hospital: '',
    doctorName: '',
    doctorContact: '',
    urgencyLevel: 'medium',

    // Common fields
    phone: user?.phone || '',
    address: user?.location || '',
    emergencyContact: '',
  });

  const [isLoading, setIsLoading] = useState(false);
  const [step, setStep] = useState(1);

  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const interval = setInterval(() => {
      saveDraft();
    }, 30000);

    return () => clearInterval(interval);
  }, [formData]);

  const handleChange = (e) => {
    const {
      name,
      value,
      type,
      checked,
    } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox'
        ? checked
        : value,
    }));
  };

  const saveDraft = async () => {
    // The current Spring Boot backend does not expose
    // /api/users/save-draft.
    // Keep this disabled until a backend draft endpoint exists.
  };

  const handleNext = () => {
    if (step < 2) {
      setStep(step + 1);
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setStep(step - 1);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setIsLoading(true);

    try {
      /*
       * Backend endpoint:
       * PUT /api/users/{id}/profile
       *
       * UserUpdateRequestDto accepts:
       * name
       * phone
       * bloodGroup
       * location
       * availableForDonation
       */

      const response = await fetch(
        `/api/users/${user.id}/profile`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
          body: JSON.stringify({
            name: user.name,
            phone: formData.phone,
            bloodGroup: user.bloodGroup,
            location: formData.address,
            availableForDonation:
              user.role === 'donor'
                ? formData.isAvailable
                : user.availableForDonation,
          }),
        }
      );

      if (response.ok) {
        const updatedUser = await response.json();

        /*
         * IMPORTANT:
         *
         * Backend returns:
         * profileCompleted
         *
         * Frontend uses:
         * profileComplete
         *
         * Therefore we MUST update profileComplete here.
         */
        updateUser({
          ...updatedUser,
          profileComplete: true,
        });

        toast({
          title: 'Profile Complete',
          description:
            'Your profile has been set up successfully!',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });

        const dashboardPath =
          user.role === 'donor'
            ? '/donor-dashboard'
            : '/recipient-dashboard';

        navigate(dashboardPath, {
          replace: true,
        });
      } else {
        let message =
          'Failed to complete profile setup';

        try {
          const data = await response.json();

          message =
            data.message ||
            data.error ||
            message;

          if (data.fieldErrors) {
            const fieldMessages =
              Object.values(data.fieldErrors);

            if (fieldMessages.length > 0) {
              message =
                fieldMessages.join(', ');
            }
          }
        } catch {
          message =
            `Profile setup failed with HTTP ${response.status}`;
        }

        throw new Error(message);
      }
    } catch (error) {
      console.error(
        'Profile setup failed:',
        error
      );

      toast({
        title: 'Setup Failed',
        description:
          error.message ||
          'Failed to complete profile setup',
        status: 'error',
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const progressValue =
    (step / 2) * 100;

  return (
    <Container maxW="2xl" py={12}>
      <VStack spacing={8}>

        <VStack
          spacing={4}
          textAlign="center"
        >
          <Heading size="lg">
            Complete Your Profile
          </Heading>

          <Text color="gray.600">
            Let's set up your {user?.role} profile
            to get started
          </Text>

          <Progress
            value={progressValue}
            colorScheme="primary"
            w="full"
          />
        </VStack>

        <Card w="full">
          <CardBody p={8}>
            <form onSubmit={handleSubmit}>
              <Stack spacing={6}>

                {/* STEP 1 */}
                {step === 1 && (
                  <>
                    <VStack
                      align="start"
                      spacing={4}
                    >
                      <HStack>
                        <Heading size="md">
                          Step 1: Basic Information
                        </Heading>

                        <Badge colorScheme="primary">
                          {user?.role}
                        </Badge>
                      </HStack>
                    </VStack>

                    <FormControl isRequired>
                      <FormLabel>
                        Phone Number
                      </FormLabel>

                      <Input
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        placeholder="Enter your phone number"
                        isDisabled={isLoading}
                      />
                    </FormControl>

                    <FormControl isRequired>
                      <FormLabel>
                        Address
                      </FormLabel>

                      <Textarea
                        name="address"
                        value={formData.address}
                        onChange={handleChange}
                        placeholder="Enter your complete address"
                        rows={3}
                        isDisabled={isLoading}
                      />
                    </FormControl>

                    <FormControl isRequired>
                      <FormLabel>
                        Emergency Contact
                      </FormLabel>

                      <Input
                        name="emergencyContact"
                        value={formData.emergencyContact}
                        onChange={handleChange}
                        placeholder="Emergency contact number"
                        isDisabled={isLoading}
                      />
                    </FormControl>

                    <Button
                      type="button"
                      onClick={handleNext}
                      colorScheme="primary"
                      size="lg"
                    >
                      Continue
                    </Button>
                  </>
                )}

                {/* STEP 2 - DONOR */}
                {step === 2 &&
                  user?.role === 'donor' && (
                    <>
                      <VStack
                        align="start"
                        spacing={4}
                      >
                        <Heading size="md">
                          Step 2: Donor Information
                        </Heading>
                      </VStack>

                      <FormControl>
                        <FormLabel>
                          Medical Conditions
                        </FormLabel>

                        <Textarea
                          name="medicalConditions"
                          value={
                            formData.medicalConditions
                          }
                          onChange={handleChange}
                          placeholder="List any medical conditions (optional)"
                          rows={3}
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <FormControl>
                        <FormLabel>
                          Current Medications
                        </FormLabel>

                        <Textarea
                          name="medications"
                          value={formData.medications}
                          onChange={handleChange}
                          placeholder="List current medications (optional)"
                          rows={3}
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <FormControl>
                        <FormLabel>
                          Last Donation Date
                        </FormLabel>

                        <Input
                          name="lastDonationDate"
                          type="date"
                          value={
                            formData.lastDonationDate
                          }
                          onChange={handleChange}
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <FormControl
                        display="flex"
                        alignItems="center"
                      >
                        <FormLabel mb="0">
                          Available for Donations
                        </FormLabel>

                        <Switch
                          name="isAvailable"
                          isChecked={
                            formData.isAvailable
                          }
                          onChange={handleChange}
                          colorScheme="primary"
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <HStack spacing={4}>
                        <Button
                          type="button"
                          onClick={handleBack}
                          variant="outline"
                          size="lg"
                          isDisabled={isLoading}
                        >
                          Back
                        </Button>

                        <Button
                          type="submit"
                          colorScheme="primary"
                          size="lg"
                          isLoading={isLoading}
                          loadingText="Setting up..."
                          flex={1}
                        >
                          Complete Setup
                        </Button>
                      </HStack>
                    </>
                  )}

                {/* STEP 2 - RECIPIENT */}
                {step === 2 &&
                  user?.role === 'recipient' && (
                    <>
                      <VStack
                        align="start"
                        spacing={4}
                      >
                        <Heading size="md">
                          Step 2: Recipient Information
                        </Heading>
                      </VStack>

                      <FormControl isRequired>
                        <FormLabel>
                          Preferred Hospital
                        </FormLabel>

                        <Input
                          name="hospital"
                          value={formData.hospital}
                          onChange={handleChange}
                          placeholder="Enter hospital name"
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <FormControl isRequired>
                        <FormLabel>
                          Doctor Name
                        </FormLabel>

                        <Input
                          name="doctorName"
                          value={formData.doctorName}
                          onChange={handleChange}
                          placeholder="Enter doctor's name"
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <FormControl isRequired>
                        <FormLabel>
                          Doctor Contact
                        </FormLabel>

                        <Input
                          name="doctorContact"
                          value={formData.doctorContact}
                          onChange={handleChange}
                          placeholder="Doctor's phone number"
                          isDisabled={isLoading}
                        />
                      </FormControl>

                      <FormControl isRequired>
                        <FormLabel>
                          Default Urgency Level
                        </FormLabel>

                        <Select
                          name="urgencyLevel"
                          value={
                            formData.urgencyLevel
                          }
                          onChange={handleChange}
                          isDisabled={isLoading}
                        >
                          <option value="low">
                            Low - Planned Surgery
                          </option>

                          <option value="medium">
                            Medium - Within 48 hours
                          </option>

                          <option value="high">
                            High - Within 24 hours
                          </option>

                          <option value="critical">
                            Critical - Immediate
                          </option>
                        </Select>
                      </FormControl>

                      <HStack spacing={4}>
                        <Button
                          type="button"
                          onClick={handleBack}
                          variant="outline"
                          size="lg"
                          isDisabled={isLoading}
                        >
                          Back
                        </Button>

                        <Button
                          type="submit"
                          colorScheme="primary"
                          size="lg"
                          isLoading={isLoading}
                          loadingText="Setting up..."
                          flex={1}
                        >
                          Complete Setup
                        </Button>
                      </HStack>
                    </>
                  )}

              </Stack>
            </form>
          </CardBody>
        </Card>

      </VStack>
    </Container>
  );
}

export default ProfileSetupPage;