
import {
  Box,
  Button,
  Container,
  FormControl,
  FormLabel,
  Heading,
  Stack,
  Text,
  useToast,
  VStack,
  Card,
  CardBody,
  CardHeader,
  Switch,
  HStack,
  Badge,
  Grid,
  GridItem,
  Alert,
  AlertIcon,
  Icon,
  Progress,
} from '@chakra-ui/react';

import {
  FiHeart,
  FiMapPin,
  FiClock,
  FiUser,
} from 'react-icons/fi';

import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useSocket } from '../../context/SocketContext';

function DonorDashboard() {
  const { user, updateUser } = useAuth();
  const { notifications } = useSocket();

  const [isAvailable, setIsAvailable] = useState(
    user?.isAvailable ?? user?.availableForDonation ?? true
  );

  const [nearbyRequests, setNearbyRequests] = useState([]);
  const [donationHistory, setDonationHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const toast = useToast();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  /*
   * Common authorization header.
   */
  const getAuthHeaders = () => ({
    Authorization: `Bearer ${localStorage.getItem('token')}`,
  });

  /*
   * Load:
   *
   * 1. Active blood requests
   * 2. Donor donation history
   *
   * The active blood-request endpoint is the Spring Boot
   * endpoint already implemented in BloodRequestController.
   */
  const fetchDashboardData = async () => {
    setIsLoading(true);

    try {
      const [requestsRes, historyRes] = await Promise.all([
        /*
         * Spring Boot:
         * GET /api/blood-requests/active
         */
        fetch('/api/blood-requests/active', {
          headers: getAuthHeaders(),
        }),

        /*
         * Donor history.
         *
         * If the backend endpoint is not implemented yet,
         * we simply keep the history empty rather than
         * breaking the complete dashboard.
         */
        fetch(`/api/donors/${user?.id}/profile`, {
          headers: getAuthHeaders(),
        }),
      ]);

      if (requestsRes.ok) {
        const requests = await requestsRes.json();

        /*
         * The backend returns BloodRequestResponseDto.
         *
         * We only show requests that are still open for
         * donor responses.
         */
        const activeRequests = requests.filter(
          (request) =>
            request.status === 'PENDING' ||
            request.status === 'MATCHED'
        );

        setNearbyRequests(activeRequests);
      } else {
        console.error(
          'Failed to load blood requests:',
          requestsRes.status
        );

        setNearbyRequests([]);
      }

      /*
       * Donor profile contains donation totals but not
       * individual donation-history records.
       *
       * Keep the history section empty until the dedicated
       * history endpoint is implemented.
       */
      if (historyRes.ok) {
        const profile = await historyRes.json();

        if (profile && profile.donations) {
          setDonationHistory(profile.donations);
        } else {
          setDonationHistory([]);
        }
      } else {
        setDonationHistory([]);
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

  /*
   * Toggle donor availability.
   *
   * IMPORTANT:
   * The current backend does not expose
   * /api/users/toggle-availability.
   *
   * Therefore we update the local frontend state here.
   * The profile endpoint can be used later if you want
   * persistent availability changes.
   */
  const toggleAvailability = async () => {
    const newValue = !isAvailable;

    setIsAvailable(newValue);

    updateUser({
      isAvailable: newValue,
      availableForDonation: newValue,
    });

    toast({
      title: 'Availability Updated',
      description: `You are now ${
        newValue ? 'available' : 'unavailable'
      } for donations`,
      status: 'success',
      duration: 3000,
      isClosable: true,
    });
  };

  /*
   * Donor accepts or declines a blood request.
   *
   * Spring Boot endpoint:
   * POST /api/blood-requests/{id}/respond
   *
   * Backend expects:
   * {
   *   "responseType": "ACCEPT"
   * }
   */
  const respondToRequest = async (
    requestId,
    responseType
  ) => {
    try {
      const res = await fetch(
        `/api/blood-requests/${requestId}/respond`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
          body: JSON.stringify({
            responseType: responseType.toUpperCase(),
          }),
        }
      );

      if (res.ok) {
        toast({
          title: 'Response Sent',
          description:
            responseType === 'accept'
              ? 'You have accepted the blood request.'
              : 'You have declined the blood request.',
          status: 'success',
          duration: 3000,
          isClosable: true,
        });

        /*
         * Reload requests so the accepted/declined
         * request is reflected immediately.
         */
        await fetchDashboardData();
      } else {
        let message =
          'Failed to send your response.';

        try {
          const data = await res.json();

          message =
            data.message ||
            data.error ||
            message;
        } catch {
          // Keep default message.
        }

        throw new Error(message);
      }
    } catch (error) {
      console.error(
        'Response failed:',
        error
      );

      toast({
        title: 'Response Failed',
        description:
          error.message ||
          'Failed to send response.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    }
  };

  const getUrgencyColor = (urgency) => {
    if (!urgency) return 'gray';

    switch (urgency.toUpperCase()) {
      case 'CRITICAL':
        return 'red';

      case 'HIGH':
        return 'orange';

      case 'MEDIUM':
        return 'yellow';

      case 'LOW':
        return 'green';

      default:
        return 'gray';
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
      <VStack
        spacing={8}
        align="stretch"
      >

        {/* Header */}
        <Box>
          <Heading
            size="lg"
            mb={2}
          >
            Welcome back, {user?.name}!
          </Heading>

          <Text color="gray.600">
            Thank you for being a life-saver.
            Here's what's happening in your area.
          </Text>
        </Box>

        {/* Availability */}
        <Card>
          <CardBody>
            <HStack
              justify="space-between"
              align="center"
            >
              <VStack
                align="start"
                spacing={1}
              >
                <Text fontWeight="medium">
                  Donation Availability
                </Text>

                <Text
                  fontSize="sm"
                  color="gray.600"
                >
                  Toggle to receive blood donation
                  requests
                </Text>
              </VStack>

              <FormControl
                display="flex"
                alignItems="center"
                w="auto"
              >
                <Switch
                  id="availability"
                  isChecked={isAvailable}
                  onChange={toggleAvailability}
                  colorScheme="primary"
                  size="lg"
                />

                <FormLabel
                  htmlFor="availability"
                  ml={3}
                  mb="0"
                >
                  <Badge
                    colorScheme={
                      isAvailable
                        ? 'green'
                        : 'gray'
                    }
                  >
                    {isAvailable
                      ? 'Available'
                      : 'Unavailable'}
                  </Badge>
                </FormLabel>
              </FormControl>
            </HStack>
          </CardBody>
        </Card>

        {/* Main Grid */}
        <Grid
          templateColumns={{
            base: '1fr',
            lg: 'repeat(2, 1fr)',
          }}
          gap={8}
        >

          {/* Nearby Blood Requests */}
          <GridItem>
            <Card h="full">
              <CardHeader>
                <HStack>
                  <Icon
                    as={FiMapPin}
                    color="primary.500"
                  />

                  <Heading size="md">
                    Nearby Blood Requests
                  </Heading>

                  <Badge>
                    {nearbyRequests.length}
                  </Badge>
                </HStack>
              </CardHeader>

              <CardBody>
                {nearbyRequests.length > 0 ? (
                  <VStack spacing={4}>
                    {nearbyRequests.map(
                      (request) => (
                        <Card
                          key={request.id}
                          w="full"
                          variant="outline"
                        >
                          <CardBody p={4}>
                            <Stack spacing={3}>

                              {/* Blood Group + Status */}
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

                              {/* Hospital */}
                              <Text fontWeight="medium">
                                {request.hospital ||
                                  'Hospital not specified'}
                              </Text>

                              {/* Units */}
                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                {request.unitsRequired}{' '}
                                units needed
                              </Text>

                              {/* Location */}
                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                <Icon
                                  as={FiMapPin}
                                  mr={1}
                                />

                                {request.location ||
                                  'Location not specified'}
                              </Text>

                              {/* Urgency */}
                              <HStack>
                                <Text
                                  fontSize="sm"
                                  color="gray.600"
                                >
                                  Urgency:
                                </Text>

                                <Badge
                                  colorScheme={getUrgencyColor(
                                    request.urgency
                                  )}
                                >
                                  {request.urgency ||
                                    'Not specified'}
                                </Badge>
                              </HStack>

                              {/* Required Date */}
                              {request.requiredDate && (
                                <Text
                                  fontSize="sm"
                                  color="gray.500"
                                >
                                  <Icon
                                    as={FiClock}
                                    mr={1}
                                  />

                                  Required by:{' '}
                                  {new Date(
                                    request.requiredDate
                                  ).toLocaleDateString()}
                                </Text>
                              )}

                              {/* Created Date */}
                              {request.createdAt && (
                                <Text
                                  fontSize="xs"
                                  color="gray.500"
                                >
                                  Request created:{' '}
                                  {new Date(
                                    request.createdAt
                                  ).toLocaleDateString()}
                                </Text>
                              )}

                              {/* Buttons */}
                              <HStack spacing={2}>
                                <Button
                                  size="sm"
                                  colorScheme="primary"
                                  onClick={() =>
                                    respondToRequest(
                                      request.id,
                                      'accept'
                                    )
                                  }
                                >
                                  Accept
                                </Button>

                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() =>
                                    respondToRequest(
                                      request.id,
                                      'decline'
                                    )
                                  }
                                >
                                  Decline
                                </Button>
                              </HStack>

                            </Stack>
                          </CardBody>
                        </Card>
                      )
                    )}
                  </VStack>
                ) : (
                  <Alert status="info">
                    <AlertIcon />

                    No blood requests in your
                    area at the moment.
                  </Alert>
                )}
              </CardBody>
            </Card>
          </GridItem>

          {/* Notifications */}
          <GridItem>
            <Card h="full">
              <CardHeader>
                <HStack>
                  <Icon
                    as={FiUser}
                    color="primary.500"
                  />

                  <Heading size="md">
                    Recent Notifications
                  </Heading>

                  <Badge>
                    {notifications?.length || 0}
                  </Badge>
                </HStack>
              </CardHeader>

              <CardBody>
                {notifications &&
                notifications.length > 0 ? (
                  <VStack spacing={4}>
                    {notifications
                      .slice(0, 5)
                      .map((notification) => (
                        <Card
                          key={
                            notification.id ||
                            notification.timestamp
                          }
                          w="full"
                          variant="outline"
                        >
                          <CardBody p={4}>
                            <Stack spacing={2}>

                              <Text
                                fontWeight="medium"
                                fontSize="sm"
                              >
                                {notification.title}
                              </Text>

                              <Text
                                fontSize="sm"
                                color="gray.600"
                              >
                                {notification.message}
                              </Text>

                              {notification.timestamp && (
                                <Text
                                  fontSize="xs"
                                  color="gray.500"
                                >
                                  {new Date(
                                    notification.timestamp
                                  ).toLocaleString()}
                                </Text>
                              )}

                            </Stack>
                          </CardBody>
                        </Card>
                      ))}
                  </VStack>
                ) : (
                  <Alert status="info">
                    <AlertIcon />

                    No new notifications.
                  </Alert>
                )}
              </CardBody>
            </Card>
          </GridItem>

        </Grid>

        {/* Donation History */}
        <Card>
          <CardHeader>
            <HStack>
              <Icon
                as={FiHeart}
                color="primary.500"
              />

              <Heading size="md">
                Recent Donations
              </Heading>
            </HStack>
          </CardHeader>

          <CardBody>
            {donationHistory.length > 0 ? (
              <Grid
                templateColumns={{
                  base: '1fr',
                  md: 'repeat(2, 1fr)',
                  lg: 'repeat(3, 1fr)',
                }}
                gap={4}
              >
                {donationHistory
                  .slice(0, 6)
                  .map((donation) => (
                    <Card
                      key={
                        donation.id ||
                        donation._id
                      }
                      variant="outline"
                    >
                      <CardBody p={4}>
                        <Stack spacing={2}>

                          <HStack
                            justify="space-between"
                          >
                            <Badge colorScheme="primary">
                              {donation.bloodGroup ||
                                user?.bloodGroup}
                            </Badge>

                            <Badge colorScheme="green">
                              Completed
                            </Badge>
                          </HStack>

                          <Text
                            fontWeight="medium"
                            fontSize="sm"
                          >
                            {donation.hospital ||
                              'Donation'}
                          </Text>

                          <Text
                            fontSize="sm"
                            color="gray.600"
                          >
                            {donation.units ||
                              donation.unitsDonated ||
                              1}{' '}
                            units donated
                          </Text>

                          {donation.donationDate && (
                            <Text
                              fontSize="xs"
                              color="gray.500"
                            >
                              {new Date(
                                donation.donationDate
                              ).toLocaleDateString()}
                            </Text>
                          )}

                        </Stack>
                      </CardBody>
                    </Card>
                  ))}
              </Grid>
            ) : (
              <Alert status="info">
                <AlertIcon />

                No donation history available.
              </Alert>
            )}
          </CardBody>
        </Card>

      </VStack>
    </Container>
  );
}

export default DonorDashboard;