// Module 9: a small adapter layer between the new backend's field names
// and the field names the EXISTING frontend was already written against.
//
// Most of the gap closes for free: Role and BloodGroup are configured
// on the backend (see entity/Role.java, entity/BloodGroup.java) to
// serialize exactly the way this frontend already expects
// ("donor"/"recipient"/"admin", "A+"/"O-"/...). What's left is a handful
// of renamed fields - rather than hunting through every page that reads
// `user.profileComplete`, we normalize the shape ONCE, right where the
// user object enters the app (AuthContext), and every existing page
// keeps working unmodified.
export function normalizeUser(backendUser) {
  if (!backendUser) return backendUser;
  return {
    ...backendUser,
    _id: backendUser.id, // the original app (MongoDB) used _id; some list/table code may still reference it
    profileComplete: backendUser.profileCompleted,
    isAvailable: backendUser.availableForDonation,
  };
}
