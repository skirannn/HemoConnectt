import { useState } from 'react';
import { Container, VStack, Heading, Text, FormControl, FormLabel, Input, Button, useToast, Card, CardBody, Link } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import * as authApi from '../../services/authApi';

// Module 9: the phone-based OTP option from the original page was
// removed here to match Module 2's backend, which only ever implemented
// the email path for real (see docs/modules/auth.md - the original
// project's "phone" option didn't actually send an SMS, it used the same
// in-memory OTP logic either way, so nothing real was lost).
function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const toast = useToast();

  const sendOtp = async () => {
    setLoading(true);
    try {
      await authApi.sendOtp(email);
      setOtpSent(true);
      toast({ title: 'OTP sent', description: 'Check the server console (local dev has no real email provider yet).', status: 'success', duration: 4000 });
    } catch (error) {
      toast({ title: 'Failed to send OTP', description: error.message, status: 'error', duration: 4000 });
    }
    setLoading(false);
  };

  const resetPassword = async () => {
    setLoading(true);
    try {
      await authApi.resetPassword(email, otp, newPassword);
      toast({ title: 'Password reset successful', status: 'success', duration: 3000 });
      setOtpSent(false);
      setOtp('');
      setNewPassword('');
    } catch (error) {
      toast({ title: 'Failed to reset', description: error.message, status: 'error', duration: 4000 });
    }
    setLoading(false);
  };

  return (
    <Container maxW="md" py={12}>
      <VStack spacing={8}>
        <VStack spacing={2} textAlign="center">
          <Heading size="lg">Forgot Password</Heading>
          <Text color="gray.600">Reset your password using an OTP sent to your email</Text>
        </VStack>
        <Card w="full">
          <CardBody p={8}>
            <VStack spacing={5} align="stretch">
              <FormControl>
                <FormLabel>Email</FormLabel>
                <Input placeholder="Enter your registered email" value={email} onChange={e => setEmail(e.target.value)} isDisabled={otpSent} />
              </FormControl>
              {!otpSent ? (
                <Button colorScheme="primary" onClick={sendOtp} isLoading={loading}>Send OTP</Button>
              ) : (
                <>
                  <FormControl>
                    <FormLabel>OTP</FormLabel>
                    <Input placeholder="Enter OTP" value={otp} onChange={e => setOtp(e.target.value)} />
                  </FormControl>
                  <FormControl>
                    <FormLabel>New Password</FormLabel>
                    <Input type="password" placeholder="Enter new password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
                  </FormControl>
                  <Button colorScheme="primary" onClick={resetPassword} isLoading={loading}>Reset Password</Button>
                </>
              )}
              <Text>
                Remembered your password? <Link as={RouterLink} to="/login" color="primary.500">Back to login</Link>
              </Text>
            </VStack>
          </CardBody>
        </Card>
      </VStack>
    </Container>
  );
}

export default ForgotPasswordPage;
