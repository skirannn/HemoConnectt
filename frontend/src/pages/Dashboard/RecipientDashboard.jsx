import {
  Container,
  Grid,
  GridItem,
  Card,
  CardBody,
  CardHeader,
  Heading,
  Text,
  Button,
  VStack,
  HStack,
  Badge,
  Box,
  useToast,
  Alert,
  AlertIcon,
  Stack,
  Icon,
  Progress,
  Divider,
} from '@chakra-ui/react';

import {
  FiPlus,
  FiMapPin,
  FiClock,
  FiList,
  FiPhone,
  FiUser,
  FiCheckCircle,
} from 'react-icons/fi';

import { useState, useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

function RecipientDashboard() {
  const { user } = useAuth();

  const [activeRequests, setActiveRequests] = useState([]);
  const [requestHistory, setRequestHistory] = useState([]);
  const [nearbyDonors, setNearbyDonors] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [confirmingDonorId, setConfirmingDonorId] = useState(null);

  const toast = useToast();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const getAuthHeaders = () => ({
    Authorization: `Bearer ${localStorage.getItem('token')}`,
  });

  const fetchDashboardData = async () => {
    setIsLoading(true);

    try {
      const [requestsRes, donorsRes] = await Promise.all([
        fetch('/api/blood-requests/mine', {
          headers: getAuthHeaders(),
        }),

        fetch('/api/donors/nearby', {
          headers: getAuthHeaders(),
        }),
      ]);

      if (requestsRes.ok) {
        const allRequests = await requestsRes.json();

        const active = allRequests.filter(
          (request) =>
            request.status !== 'FULFILLED' &&
            request.status !== 'CANCELLED'
        );

        const history = allRequests.filter(
          (request) =>
            request.status === 'FULFILLED' ||
            request.status === 'CANCELLED'
        );

        setActiveRequests(active);
        setRequestHistory(history);
      } else {
        console.error(
          'Failed to load requests:',
          requestsRes.status
        );
      }

      if (donorsRes.ok) {
        const donors = await donorsRes.json();
        setNearbyDonors(donors);
      } else {
        console.error(
          'Failed to load nearby donors:',
          donorsRes.status
        );
      }
    } catch (error) {
      console.error(
        'Failed to fetch dashboard data:',
        error
      );

      toast({
        title: 'Dashboard Error',
        description:
          'Unable to load the latest dashboard data.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const cancelRequest = async (requestId) => {
    try {
      const response = await fetch(
        `/api/blood-requests/${requestId}/cancel`,
        {
          method: 'POST',
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        let message = 'Failed to cancel the request.';

        try {
          const data = await response.json();

          message =
            data.message ||
            data.error ||
            message;
        } catch {
          // Keep default message.
        }

        throw new Error(message);
      }

      toast({
        title: 'Request Cancelled',
        description:
          'Your blood request has been cancelled.',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });

      await fetchDashboardData();
    } catch (error) {
      console.error(
        'Cancellation failed:',
        error
      );

      toast({
        title: 'Cancellation Failed',
        description:
          error.message ||
          'Failed to cancel the request.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    }
  };

  const confirmDonor = async (requestId, donorId) => {
    setConfirmingDonorId(donorId);

    try {
      const response = await fetch(
        `/api/blood-requests/${requestId}/confirm`,
        {
          method: 'POST',
          headers: {
            ...getAuthHeaders(),
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            donorId: donorId,
          }),
        }
      );

      if (!response.ok) {
        let message = 'Failed to confirm donor.';

        try {
          const data = await response.json();

          message =
            data.message ||
            data.error ||
            message;
        } catch {
          // Keep default message.
        }

        throw new Error(message);
      }

      toast({
        title: 'Donor Confirmed',
        description:
          'The donor has been confirmed for your blood request.',
        status: 'success',
        duration: 4000,
        isClosable: true,
      });

      await fetchDashboardData();
    } catch (error) {
      console.error(
        'Donor confirmation failed:',
        error
      );

      toast({
        title: 'Confirmation Failed',
        description:
          error.message ||
          'Failed to confirm donor.',
        status: 'error',
        duration: 4000,
        isClosable: true,
      });
    } finally {
      setConfirmingDonorId(null);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'yellow';

      case 'MATCHED':
        return 'blue';

      case 'CONFIRMED':
        return 'purple';

      case 'FULFILLED':
        return 'green';

      case 'CANCELLED':
        return 'red';

      default:
        return 'gray';
    }
  };

  const getStatusProgress = (status) => {
    switch (status) {
      case 'PENDING':
        return 25;

      case 'MATCHED':
        return 50;

      case 'CONFIRMED':
        return 75;

      case 'FULFILLED':
        return 100;

      case 'CANCELLED':
        return 0;

      default:
        return 0;
    }
  };

  const getAcceptedResponses = (request) => {
    if (!request.responses) {
      return [];
    }

    return request.responses.filter(
      (response) =>
        response.responseType === 'ACCEPT'
    );
  };

  if (isLoading) {
    return (
      <Container maxW="6xl" py={8}>
        <Text>
          Loading dashboard...
        </Text>
      </Container>
    );
  }

  return (
    <Container maxW="6xl" py={8}>
      <VStack spacing={8} align="stretch">

        {/* Header */}
        <HStack
          justify="space-between"
          align="start"
        >
          <Box>
            <Heading size="lg" mb={2}>
              Welcome, {user?.name}!
            </Heading>

            <Text color="gray.600">
              Manage your blood requests and track
              their progress
            </Text>
          </Box>

          <Button
            as={RouterLink}
            to="/request-blood"
            colorScheme="primary"
            leftIcon={<Icon as={FiPlus} />}
            size="lg"
          >
            New Request
          </Button>
        </HStack>

        {/* Main Dashboard */}
        <Grid
          templateColumns={{
            base: '1fr',
            lg: 'repeat(2, 1fr)',
          }}
          gap={8}
        >

          {/* Active Requests */}
          <GridItem>
            <Card h="full">
              <CardHeader>
                <HStack>
                  <Icon
                    as={FiClock}
                    color="primary.500"
                  />

                  <Heading size="md">
                    Active Requests
                  </Heading>

                  <Badge>
                    {activeRequests.length}
                  </Badge>
                </HStack>
              </CardHeader>

              <CardBody>
                {activeRequests.length > 0 ? (
                  <VStack spacing={6}>
                    {activeRequests.map((request) => {
                      const acceptedDonors =
                        getAcceptedResponses(request);

                      return (
                        <Card
                          key={request.id}
                          w="full"
                          variant="outline"
                        >
                          <CardBody p={5}>
                            <Stack spacing={4}>

                              {/* Request Header */}
                              <HStack
                                justify="space-between"
                              >
                                <Badge
                                  colorScheme="primary"
                                  fontSize="sm"
                                  px={2}
                                  py={1}
                                >
                                  {request.bloodGroup}
                                </Badge>

                                <Badge
                                  colorScheme={getStatusColor(
                                    request.status
                                  )}
                                >
                                  {request.status}
                                </Badge>
                              </HStack>

                              <Text
                                fontWeight="bold"
                                fontSize="lg"
                              >
                                {request.hospital ||
                                  'Hospital not specified'}
                              </Text>

                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                {request.unitsRequired}{' '}
                                units needed
                              </Text>

                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                <Icon
                                  as={FiMapPin}
                                  mr={1}
                                />
                                {request.location}
                              </Text>

                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                Required by:{' '}
                                {request.requiredDate}
                              </Text>

                              {/* Progress */}
                              <Box>
                                <HStack
                                  justify="space-between"
                                  mb={2}
                                >
                                  <Text fontSize="sm">
                                    Progress
                                  </Text>

                                  <Text fontSize="sm">
                                    {request.status}
                                  </Text>
                                </HStack>

                                <Progress
                                  value={getStatusProgress(
                                    request.status
                                  )}
                                  colorScheme="primary"
                                  size="sm"
                                  rounded="full"
                                />
                              </Box>

                              {/* Donor Responses */}
                              {acceptedDonors.length > 0 && (
                                <>
                                  <Divider />

                                  <Box>
                                    <HStack mb={3}>
                                      <Icon
                                        as={FiCheckCircle}
                                        color="green.500"
                                      />

                                      <Text
                                        fontWeight="bold"
                                        color="green.600"
                                      >
                                        Donor Accepted Your
                                        Request
                                      </Text>
                                    </HStack>

                                    <VStack
                                      spacing={4}
                                      align="stretch"
                                    >
                                      {acceptedDonors.map(
                                        (donor) => (
                                          <Card
                                            key={donor.id}
                                            variant="outline"
                                            bg="green.50"
                                          >
                                            <CardBody p={4}>
                                              <Stack spacing={3}>

                                                <HStack>
                                                  <Icon
                                                    as={FiUser}
                                                  />

                                                  <Text
                                                    fontWeight="bold"
                                                  >
                                                    {donor.donorName}
                                                  </Text>

                                                  <Badge
                                                    colorScheme="green"
                                                  >
                                                    ACCEPTED
                                                  </Badge>
                                                </HStack>

                                                {donor.donorBloodGroup && (
                                                  <Text fontSize="sm">
                                                    <strong>
                                                      Blood Group:
                                                    </strong>{' '}
                                                    {donor.donorBloodGroup}
                                                  </Text>
                                                )}

                                                {donor.donorLocation && (
                                                  <Text fontSize="sm">
                                                    <Icon
                                                      as={FiMapPin}
                                                      mr={1}
                                                    />
                                                    <strong>
                                                      Location:
                                                    </strong>{' '}
                                                    {donor.donorLocation}
                                                  </Text>
                                                )}

                                                {donor.donorPhone && (
                                                  <Text fontSize="sm">
                                                    <Icon
                                                      as={FiPhone}
                                                      mr={1}
                                                    />
                                                    <strong>
                                                      Phone:
                                                    </strong>{' '}
                                                    {donor.donorPhone}
                                                  </Text>
                                                )}

                                                {donor.responseMessage && (
                                                  <Text
                                                    fontSize="sm"
                                                    color="gray.600"
                                                  >
                                                    Message:{' '}
                                                    {donor.responseMessage}
                                                  </Text>
                                                )}

                                                {/* Confirm Donor */}
                                                {request.status ===
                                                  'MATCHED' && (
                                                  <Button
                                                    colorScheme="green"
                                                    size="sm"
                                                    isLoading={
                                                      confirmingDonorId ===
                                                      donor.donorId
                                                    }
                                                    loadingText="Confirming..."
                                                    onClick={() =>
                                                      confirmDonor(
                                                        request.id,
                                                        donor.donorId
                                                      )
                                                    }
                                                  >
                                                    Confirm Donor
                                                  </Button>
                                                )}

                                              </Stack>
                                            </CardBody>
                                          </Card>
                                        )
                                      )}
                                    </VStack>
                                  </Box>
                                </>
                              )}

                              {/* Waiting for donor */}
                              {request.status ===
                                'PENDING' &&
                                acceptedDonors.length ===
                                  0 && (
                                  <Alert status="info">
                                    <AlertIcon />

                                    Waiting for a matching
                                    donor to accept your
                                    request.
                                  </Alert>
                                )}

                              {/* Confirmed Donor */}
                              {request.status ===
                                'CONFIRMED' &&
                                request.confirmedDonorName && (
                                  <>
                                    <Divider />

                                    <Alert status="success">
                                      <AlertIcon />

                                      <Box>
                                        <Text
                                          fontWeight="bold"
                                        >
                                          Donor Confirmed
                                        </Text>

                                        <Text fontSize="sm">
                                          {request.confirmedDonorName}
                                          {' '}has been confirmed
                                          for this request.
                                        </Text>
                                      </Box>
                                    </Alert>
                                  </>
                                )}

                              {/* Cancel */}
                              {request.status ===
                                'PENDING' && (
                                <Button
                                  size="sm"
                                  colorScheme="red"
                                  variant="outline"
                                  onClick={() =>
                                    cancelRequest(
                                      request.id
                                    )
                                  }
                                >
                                  Cancel Request
                                </Button>
                              )}

                            </Stack>
                          </CardBody>
                        </Card>
                      );
                    })}
                  </VStack>
                ) : (
                  <Alert status="info">
                    <AlertIcon />

                    No active requests.
                    Click "New Request" to create one.
                  </Alert>
                )}
              </CardBody>
            </Card>
          </GridItem>

          {/* Nearby Donors */}
          <GridItem>
            <Card h="full">
              <CardHeader>
                <HStack>
                  <Icon
                    as={FiMapPin}
                    color="primary.500"
                  />

                  <Heading size="md">
                    Available Donors
                  </Heading>

                  <Badge>
                    {nearbyDonors.length}
                  </Badge>
                </HStack>
              </CardHeader>

              <CardBody>
                {nearbyDonors.length > 0 ? (
                  <VStack spacing={4}>
                    {nearbyDonors
                      .slice(0, 5)
                      .map((donor) => (
                        <Card
                          key={donor.id}
                          w="full"
                          variant="outline"
                        >
                          <CardBody p={4}>
                            <Stack spacing={2}>

                              <HStack
                                justify="space-between"
                              >
                                <Text fontWeight="medium">
                                  {donor.name}
                                </Text>

                                <Badge colorScheme="primary">
                                  {donor.bloodGroup}
                                </Badge>
                              </HStack>

                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                <Icon
                                  as={FiMapPin}
                                  mr={1}
                                />
                                {donor.location}
                              </Text>

                              <Button
                                size="sm"
                                colorScheme="primary"
                                variant="outline"
                                as={RouterLink}
                                to={`/contact?donorId=${donor.id}`}
                              >
                                Contact Donor
                              </Button>

                            </Stack>
                          </CardBody>
                        </Card>
                      ))}
                  </VStack>
                ) : (
                  <Alert status="warning">
                    <AlertIcon />

                    No donors available in your
                    area right now.
                  </Alert>
                )}
              </CardBody>
            </Card>
          </GridItem>

        </Grid>

        {/* Request History */}
        <Card>
          <CardHeader>
            <HStack>
              <Icon
                as={FiList}
                color="primary.500"
              />

              <Heading size="md">
                Request History
              </Heading>
            </HStack>
          </CardHeader>

          <CardBody>
            {requestHistory.length > 0 ? (
              <Grid
                templateColumns={{
                  base: '1fr',
                  md: 'repeat(2, 1fr)',
                  lg: 'repeat(3, 1fr)',
                }}
                gap={4}
              >
                {requestHistory
                  .slice(0, 6)
                  .map((request) => (
                    <Card
                      key={request.id}
                      variant="outline"
                    >
                      <CardBody p={4}>
                        <Stack spacing={2}>

                          <HStack
                            justify="space-between"
                          >
                            <Badge colorScheme="primary">
                              {request.bloodGroup}
                            </Badge>

                            <Badge
                              colorScheme={getStatusColor(
                                request.status
                              )}
                            >
                              {request.status}
                            </Badge>
                          </HStack>

                          <Text
                            fontWeight="medium"
                            fontSize="sm"
                          >
                            {request.hospital ||
                              'Hospital not specified'}
                          </Text>

                          <Text
                            fontSize="sm"
                            color="gray.600"
                          >
                            {request.unitsRequired}{' '}
                            units
                          </Text>

                          {request.confirmedDonorName && (
                            <Text
                              fontSize="sm"
                              color="green.600"
                            >
                              Donor:{' '}
                              {request.confirmedDonorName}
                            </Text>
                          )}

                          <Text
                            fontSize="xs"
                            color="gray.500"
                          >
                            {request.createdAt
                              ? new Date(
                                  request.createdAt
                                ).toLocaleDateString()
                              : ''}
                          </Text>

                        </Stack>
                      </CardBody>
                    </Card>
                  ))}
              </Grid>
            ) : (
              <Alert status="info">
                <AlertIcon />

                No request history available.
              </Alert>
            )}
          </CardBody>
        </Card>

      </VStack>
    </Container>
  );
}

export default RecipientDashboard;